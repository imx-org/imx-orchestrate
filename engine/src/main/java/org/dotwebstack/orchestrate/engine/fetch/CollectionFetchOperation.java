package org.dotwebstack.orchestrate.engine.fetch;

import java.util.Map;
import java.util.logging.Level;
import lombok.experimental.SuperBuilder;
import org.dotwebstack.orchestrate.source.CollectionRequest;
import org.dotwebstack.orchestrate.source.FilterDefinition;
import org.reactivestreams.Publisher;
import reactor.core.publisher.SignalType;

@SuperBuilder(toBuilder = true)
final class CollectionFetchOperation extends AbstractFetchOperation {

  private final FilterDefinition filter;

  private final boolean single;

  public Publisher<Map<String, Object>> execute(Map<String, Object> input) {
    var mappedInput = inputMapper.apply(input);

    var collectionRequest = CollectionRequest.builder()
        .objectType(objectType)
        .filter(filter != null ? filter.createExpression(mappedInput) : null)
        .selectedProperties(selectedProperties)
        .build();

    var result = source.getDataRepository()
        .find(collectionRequest)
        .log(objectType.getName(), Level.INFO, SignalType.ON_NEXT)
        .flatMap(this::executeNextOperations);

    return single ? result.singleOrEmpty() : result;
  }
}
