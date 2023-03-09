package org.dotwebstack.orchestrate.engine.fetch;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Getter;
import org.dotwebstack.orchestrate.engine.OrchestrateException;
import org.dotwebstack.orchestrate.model.Property;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Getter
@Builder(toBuilder = true)
public class NextOperation {

  private final Property property;

  private final FetchOperation delegateOperation;

  private final Function<ObjectResult, Map<String, Object>> inputMapper;

  public Publisher<ObjectResult> apply(Flux<ObjectResult> resultFlux) {
    if (!property.getCardinality().isSingular()) {
      throw new OrchestrateException("Nested lists are not (yet) supported.");
    }

    // TODO: handle only distinct inputs
    if (delegateOperation instanceof CollectionFetchOperation) {
      return resultFlux.flatMap(this::fetchCollection);
    }

    return resultFlux.buffer()
        .flatMap(this::execute);
  }

  private Publisher<ObjectResult> fetchCollection(ObjectResult objectResult) {
    var inputData = inputMapper.apply(objectResult);

    if (inputData == null) {
      return Mono.just(objectResult);
    }

    var input = FetchInput.builder()
        .data(inputData)
        .build();

    return delegateOperation.execute(input)
        .singleOrEmpty()
        .map(nextResult -> objectResult.toBuilder()
            .relatedObject(property.getName(), nextResult)
            .build())
        .defaultIfEmpty(objectResult);
  }

  private Publisher<ObjectResult> execute(List<ObjectResult> objectResults) {
    var distinctInputs = objectResults.stream()
        .flatMap(objectResult -> Optional.ofNullable(inputMapper.apply(objectResult))
            .stream())
        .distinct()
        .map(data -> FetchInput.builder()
            .data(data)
            .build())
        .toList();

    if (distinctInputs.isEmpty()) {
      return Flux.fromIterable(objectResults);
    }

    if (distinctInputs.size() == 1) {
      return Mono.just(distinctInputs.get(0))
          .flatMapMany(nextInput -> delegateOperation.execute(nextInput)
              .collect(Collectors.toMap(nextResult -> nextInput.getData(), Function.identity()))
              .flatMapMany(nextResults -> combineResult(objectResults, nextResults)));
    }

    return delegateOperation.executeBatch(distinctInputs)
        .collect(Collectors.toMap(ObjectResult::getKey, Function.identity()))
        .flatMapMany(nextResults -> combineResult(objectResults, nextResults));
  }

  private Flux<ObjectResult> combineResult(List<ObjectResult> objectResults,
      Map<Map<String, Object>, ObjectResult> nextResults) {
    return Flux.fromIterable(objectResults)
        .map(objectResult -> {
          // TODO: Input mapping is now done twice
          var nextInput = inputMapper.apply(objectResult);

          if (nextInput == null) {
            return objectResult;
          }

          return Optional.ofNullable(nextResults.get(nextInput))
              .map(nextResult -> objectResult.toBuilder()
                  .relatedObject(property.getName(), nextResult)
                  .build())
              .orElse(objectResult);
        });
  }
}
