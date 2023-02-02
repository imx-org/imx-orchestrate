package org.dotwebstack.orchestrate.engine.fetch;

import java.util.Map;
import java.util.logging.Level;
import lombok.experimental.SuperBuilder;
import org.dotwebstack.orchestrate.source.CollectionRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.SignalType;

@SuperBuilder(toBuilder = true)
final class CollectionFetchOperation extends AbstractFetchOperation {

  public Flux<Map<String, Object>> execute(Map<String, Object> input) {
    var collectionRequest = CollectionRequest.builder()
        .objectType(objectType)
        .selectedFields(selectedFields)
        .build();

    return source.getDataRepository()
        .find(collectionRequest)
        .log(objectType.getName(), Level.INFO, SignalType.ON_NEXT)
        .flatMap(this::executeNextOperations);
  }
}
