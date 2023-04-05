package org.dotwebstack.orchestrate.engine.fetch;

import static java.util.function.Function.identity;
import static org.dotwebstack.orchestrate.engine.fetch.FetchUtils.cast;
import static org.dotwebstack.orchestrate.engine.fetch.FetchUtils.extractKey;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import lombok.Builder;
import lombok.Getter;
import org.dataloader.DataLoader;
import org.dataloader.DataLoaderFactory;
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
    // TODO: handle only distinct inputs
    if (delegateOperation instanceof CollectionFetchOperation) {
      return resultFlux.flatMapSequential(this::fetchCollection);
    }

    var dataLoader = createDataLoader();

    return resultFlux.doOnComplete(dataLoader::dispatch)
        .flatMapSequential(objectResult -> {
          var input = objectResult.getProperty(property.getName());

          if (input == null) {
            return Mono.just(objectResult);
          }

          if (input instanceof List<?>) {
            return Mono.fromCompletionStage(dataLoader.loadMany(cast(input)))
                .map(nestedList -> objectResult.toBuilder()
                    .nestedResult(property.getName(), CollectionResult.builder()
                        .objectResults(nestedList)
                        .build())
                    .build());
          }

          return Mono.fromCompletionStage(dataLoader.load(cast(input)))
              .map(nestedObject -> objectResult.toBuilder()
                  .nestedResult(property.getName(), nestedObject)
                  .build())
              .defaultIfEmpty(objectResult);
        });
  }

  private DataLoader<Map<String, Object>, ObjectResult> createDataLoader() {
    return DataLoaderFactory.newMappedDataLoader(objectKeys -> {
      var resultFlux = objectKeys.size() == 1 ?
          delegateOperation.execute(FetchInput.newInput(objectKeys.iterator().next())) :
          delegateOperation.executeBatch(objectKeys.stream()
              .map(FetchInput::newInput)
              .toList());

      return resultFlux.collectMap(result -> extractKey(result.getType(), result.getProperties()), identity())
          .toFuture();
    });
  }

  private Publisher<ObjectResult> fetchCollection(ObjectResult objectResult) {
    var inputData = inputMapper.apply(objectResult);

    if (inputData == null) {
      return Mono.just(objectResult);
    }

    var input = FetchInput.newInput(inputData);

    if (!property.getCardinality().isSingular()) {
      return delegateOperation.execute(input)
          .collectList()
          .map(objectResults -> objectResult.toBuilder()
              .nestedResult(property.getName(), CollectionResult.builder()
                  .objectResults(objectResults)
                  .build())
              .build());
    }

    return delegateOperation.execute(input)
        .map(nextResult -> objectResult.toBuilder()
            .nestedResult(property.getName(), nextResult)
            .build())
        .defaultIfEmpty(objectResult);
  }
}
