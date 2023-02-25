package org.dotwebstack.orchestrate.engine.fetch;

import java.util.Map;
import java.util.logging.Level;
import lombok.experimental.SuperBuilder;
import org.dotwebstack.orchestrate.source.CollectionRequest;
import org.dotwebstack.orchestrate.source.FilterDefinition;
import reactor.core.publisher.Flux;
import reactor.core.publisher.SignalType;

@SuperBuilder(toBuilder = true)
final class CollectionFetchOperation extends AbstractFetchOperation {

  private final FilterDefinition filter;

  public Flux<ObjectResult> fetch(Map<String, Object> input) {
    var collectionRequest = CollectionRequest.builder()
        .objectType(objectType)
        .filter(filter != null ? filter.createExpression(input) : null)
        .selectedProperties(selectedProperties)
        .build();

    return source.getDataRepository()
        .find(collectionRequest)
        .log(objectType.getName(), Level.INFO, SignalType.ON_NEXT)
        .map(properties -> ObjectResult.builder()
            .type(objectType)
            .properties(properties)
            .build());
  }
}
