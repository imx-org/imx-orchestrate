package nl.geostandaarden.imx.orchestrate.engine.fetch;

import java.util.List;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import nl.geostandaarden.imx.orchestrate.engine.exchange.ObjectResult;
import nl.geostandaarden.imx.orchestrate.engine.exchange.BatchRequest;
import nl.geostandaarden.imx.orchestrate.engine.exchange.ObjectRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@SuperBuilder(toBuilder = true)
public final class ObjectFetchOperation extends AbstractFetchOperation {

  public Mono<ObjectResult> fetch(FetchInput input) {
    var objectRequest = ObjectRequest.builder(model)
        .objectType(objectType.getName())
        .objectKey(input.getData())
        .selectedProperties(selectedProperties)
        .build();

    if (log.isDebugEnabled()) {
      log.debug(objectRequest.toString());
    }

    return source.getDataRepository()
        .findOne(objectRequest)
        .map(properties -> ObjectResult.builder()
            .type(objectType)
            .properties(properties)
            .build());
  }

  public Flux<ObjectResult> fetchBatch(List<FetchInput> inputs) {
    var dataRepository = source.getDataRepository();

    if (!dataRepository.supportsBatchLoading(objectType)) {
      return Flux.fromIterable(inputs)
          .flatMap(this::fetch);
    }

    var objectKeys = inputs.stream()
        .map(FetchInput::getData)
        .toList();

    var batchRequest = BatchRequest.builder(model)
        .objectType(objectType.getName())
        .objectKeys(objectKeys)
        .selectedProperties(selectedProperties)
        .build();

    if (log.isDebugEnabled()) {
      log.debug(batchRequest.toString());
    }

    return dataRepository.findBatch(batchRequest)
        .map(properties -> ObjectResult.builder()
            .type(objectType)
            .properties(properties)
            .build());
  }
}
