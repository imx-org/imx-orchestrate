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

  public Mono<ObjectResult> execute(Map<String, Object> input) {
    var mappedInput = inputMapper.apply(input);

    if (mappedInput == null) {
      return Mono.empty();
    }

    var objectKey = keyExtractor.apply(mappedInput);

    var objectRequest = ObjectRequest.builder()
        .objectType(objectType)
        .objectKey(objectKey)
        .selectedProperties(selectedProperties)
        .build();

    return source.getDataRepository()
        .findOne(objectRequest)
        .log(objectType.getName(), Level.INFO, SignalType.ON_NEXT)
        .map(properties -> ObjectResult.builder()
            .objectType(objectType)
            .objectKey(objectKey)
            .properties(properties)
            .build())
        .flatMap(this::executeNextOperations);
  }
}
