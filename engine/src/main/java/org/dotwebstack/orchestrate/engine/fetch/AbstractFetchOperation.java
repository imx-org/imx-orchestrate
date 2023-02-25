package org.dotwebstack.orchestrate.engine.fetch;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.UnaryOperator;
import lombok.Builder;
import lombok.Singular;
import lombok.experimental.SuperBuilder;
import org.dotwebstack.orchestrate.model.ObjectType;
import org.dotwebstack.orchestrate.source.SelectedProperty;
import org.dotwebstack.orchestrate.source.Source;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@SuperBuilder(toBuilder = true)
abstract class AbstractFetchOperation implements FetchOperation {

  protected final Source source;

  protected final ObjectType objectType;

  @Singular
  protected final List<SelectedProperty> selectedProperties;

  @Singular
  protected final Set<NextOperation> nextOperations;

  @Builder.Default
  protected final UnaryOperator<ObjectResult> resultMapper = UnaryOperator.identity();

  public final Flux<ObjectResult> execute(Map<String, Object> input) {
    return Flux.from(fetch(input))
        .flatMap(this::executeNextOperations)
        .map(resultMapper);
  }

  protected abstract Publisher<ObjectResult> fetch(Map<String, Object> input);

  private Mono<ObjectResult> executeNextOperations(ObjectResult objectResult) {
    if (nextOperations.isEmpty()) {
      return Mono.just(objectResult);
    }

    return Flux.fromIterable(nextOperations)
        .flatMap(nextOperation -> nextOperation.execute(objectResult))
        .collect(objectResult::toBuilder, (builder, result) -> builder.relatedObject(result.getPropertyName(),
            result.getObjectResult()))
        .map(ObjectResult.ObjectResultBuilder::build);
  }
}
