package org.dotwebstack.orchestrate.engine.fetch;

import java.util.List;
import java.util.logging.Level;
import lombok.experimental.SuperBuilder;
import org.dotwebstack.orchestrate.source.BatchRequest;
import org.dotwebstack.orchestrate.source.ObjectRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;

@SuperBuilder(toBuilder = true)
final class ObjectFetchOperation extends AbstractFetchOperation {

  public Mono<ObjectResult> fetch(FetchInput input) {
    var objectRequest = ObjectRequest.builder()
        .objectType(objectType)
        .objectKey(input.getData())
        .selectedProperties(selectedProperties)
        .build();

    return source.getDataRepository()
        .findOne(objectRequest)
        .log("Object/" + objectType.getName(), Level.INFO, SignalType.ON_NEXT)
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

    var batchRequest = BatchRequest.builder()
        .objectType(objectType)
        .objectKeys(objectKeys)
        .selectedProperties(selectedProperties)
        .build();

    return dataRepository.findBatch(batchRequest)
        .log("ObjectBatch/" + objectType.getName(), Level.INFO, SignalType.ON_NEXT)
        .map(properties -> ObjectResult.builder()
            .type(objectType)
            .properties(properties)
            .build());
  }
}
