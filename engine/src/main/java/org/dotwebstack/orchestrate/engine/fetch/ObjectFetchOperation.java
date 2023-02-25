package org.dotwebstack.orchestrate.engine.fetch;

import java.util.Map;
import java.util.logging.Level;
import lombok.experimental.SuperBuilder;
import org.dotwebstack.orchestrate.source.ObjectRequest;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;

@SuperBuilder(toBuilder = true)
final class ObjectFetchOperation extends AbstractFetchOperation {

  public Mono<ObjectResult> fetch(Map<String, Object> input) {
    var objectRequest = ObjectRequest.builder()
        .objectType(objectType)
        .objectKey(input)
        .selectedProperties(selectedProperties)
        .build();

    return source.getDataRepository()
        .findOne(objectRequest)
        .log(objectType.getName(), Level.INFO, SignalType.ON_NEXT)
        .map(properties -> ObjectResult.builder()
            .type(objectType)
            .properties(properties)
            .build());
  }
}
