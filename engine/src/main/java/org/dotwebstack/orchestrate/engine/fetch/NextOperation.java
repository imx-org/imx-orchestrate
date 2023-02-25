package org.dotwebstack.orchestrate.engine.fetch;

import java.util.Map;
import java.util.function.Function;
import lombok.Builder;
import lombok.Getter;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

@Getter
@Builder(toBuilder = true)
public class NextOperation {

  private final String propertyName;

  private final FetchOperation delegateOperation;

  private final Function<ObjectResult, Map<String, Object>> inputMapper;

  private final boolean singleResult;

  public Publisher<NextOperationResult> execute(ObjectResult objectResult) {
    var input = inputMapper.apply(objectResult);

    if (input == null) {
      return Mono.empty();
    }

    var resultPublisher = delegateOperation.execute(input);

    // TODO: Handle nested object lists
    return Mono.from(resultPublisher)
        .map(result -> new NextOperationResult(propertyName, result));
  }
}
