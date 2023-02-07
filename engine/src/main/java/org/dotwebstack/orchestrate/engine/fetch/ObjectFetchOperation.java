package org.dotwebstack.orchestrate.engine.fetch;

import java.util.Map;
import java.util.function.UnaryOperator;
import java.util.logging.Level;
import lombok.experimental.SuperBuilder;
import org.dotwebstack.orchestrate.source.ObjectRequest;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;

@SuperBuilder(toBuilder = true)
final class ObjectFetchOperation extends AbstractFetchOperation {

  private final UnaryOperator<Map<String, Object>> keyExtractor;

  public Mono<Map<String, Object>> execute(Map<String, Object> input) {
    var objectRequest = ObjectRequest.builder()
        .objectType(objectType)
        .objectKey(keyExtractor.apply(input))
        .selectedProperties(selectedProperties)
        .build();

    return source.getDataRepository()
        .findOne(objectRequest)
        .log(objectType.getName(), Level.INFO, SignalType.ON_NEXT)
        .flatMap(this::executeNextOperations);
  }
}
