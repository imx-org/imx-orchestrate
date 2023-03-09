package org.dotwebstack.orchestrate.engine.fetch;

import java.util.Map;
import java.util.function.Function;
import lombok.Builder;
import lombok.Getter;
import org.dotwebstack.orchestrate.engine.OrchestrateException;
import org.dotwebstack.orchestrate.model.Property;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Getter
@Builder(toBuilder = true)
public class NextOperation {

  private final Property property;

  private final FetchOperation delegateOperation;

  private final Function<ObjectResult, Map<String, Object>> inputMapper;

  private final boolean singleResult;

  public Publisher<ObjectResult> apply(Flux<ObjectResult> resultFlux, FetchContext context) {
    if (!property.getCardinality().isSingular()) {
      throw new OrchestrateException("Nested lists are not (yet) supported.");
    }

    return resultFlux.flatMap(objectResult -> {
      var input = inputMapper.apply(objectResult);

      if (input == null) {
        return Mono.just(objectResult);
      }

      return delegateOperation.execute(context.withInput(input))
          .map(relatedObject -> objectResult.toBuilder()
              .relatedObject(property.getName(), relatedObject)
              .build())
          .defaultIfEmpty(objectResult);
    });
  }
}
