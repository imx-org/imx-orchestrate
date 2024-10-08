package nl.geostandaarden.imx.orchestrate.engine.fetch;

import static java.util.function.Function.identity;
import static nl.geostandaarden.imx.orchestrate.engine.fetch.FetchUtils.cast;
import static nl.geostandaarden.imx.orchestrate.engine.fetch.FetchUtils.keyFromResult;
import static nl.geostandaarden.imx.orchestrate.model.ModelUtils.extractKey;

import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import nl.geostandaarden.imx.orchestrate.engine.exchange.CollectionResult;
import nl.geostandaarden.imx.orchestrate.engine.exchange.ObjectResult;
import nl.geostandaarden.imx.orchestrate.model.Property;
import nl.geostandaarden.imx.orchestrate.model.Relation;
import org.dataloader.DataLoader;
import org.dataloader.DataLoaderFactory;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Getter
@Builder(toBuilder = true)
public class NextOperation {

  private final Property property;

  private final FetchOperation delegateOperation;

  public Publisher<ObjectResult> apply(Flux<ObjectResult> resultFlux) {
    // TODO: handle only distinct inputs
    if (delegateOperation instanceof CollectionFetchOperation) {
      return resultFlux.flatMapSequential(this::fetchCollection);
    }

    var dataLoader = createDataLoader();

    return resultFlux.doOnComplete(dataLoader::dispatch)
        .flatMapSequential(objectResult -> {
          var inputValue = getInputValue(objectResult);

          if (inputValue == null) {
            return Mono.just(objectResult);
          }

          if (inputValue instanceof List<?>) {
            return Mono.fromCompletionStage(dataLoader.loadMany(cast(inputValue)))
                .map(nestedList -> objectResult.toBuilder()
                    .property(property.getName(), CollectionResult.builder()
                        .objectResults(nestedList)
                        .build())
                    .build());
          }

          return Mono.fromCompletionStage(dataLoader.load(cast(inputValue)))
              .map(nestedObject -> objectResult.toBuilder()
                  .property(property.getName(), nestedObject)
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
    var input = FetchInput.newInput(objectResult.getProperties());

    if (!property.getMultiplicity().isSingular()) {
      return delegateOperation.execute(input)
          .collectList()
          .map(objectResults -> objectResult.toBuilder()
              .property(property.getName(), CollectionResult.builder()
                  .objectResults(objectResults)
                  .build())
              .build());
    }

    return delegateOperation.execute(input)
        .map(nextResult -> objectResult.toBuilder()
            .property(property.getName(), nextResult)
            .build())
        .defaultIfEmpty(objectResult);
  }

  private Object getInputValue(ObjectResult objectResult) {
    if (property instanceof Relation relation) {
      var keyMapping = relation.getKeyMapping();

      if (keyMapping != null) {
        return keyFromResult(objectResult, keyMapping);
      }
    }

    return objectResult.getProperty(property.getName());
  }
}
