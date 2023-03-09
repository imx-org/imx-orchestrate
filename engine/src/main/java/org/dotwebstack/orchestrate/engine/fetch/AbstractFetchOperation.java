package org.dotwebstack.orchestrate.engine.fetch;

import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import lombok.Builder;
import lombok.Singular;
import lombok.experimental.SuperBuilder;
import org.dotwebstack.orchestrate.model.ObjectType;
import org.dotwebstack.orchestrate.source.SelectedProperty;
import org.dotwebstack.orchestrate.source.Source;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

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

  public final Flux<ObjectResult> execute(FetchContext context) {
    return Flux.from(fetch(context))
        .transform(resultFlux -> executeNextOperations(resultFlux, context))
        .map(resultMapper);
  }

  protected abstract Publisher<ObjectResult> fetch(FetchContext context);

  private Publisher<ObjectResult> executeNextOperations(Flux<ObjectResult> resultFlux, FetchContext context) {
    if (nextOperations.isEmpty()) {
      return resultFlux;
    }

    return Flux.fromIterable(nextOperations)
        .reduce(resultFlux, (acc, nextOperation) -> acc.transform(rf -> nextOperation.apply(rf, context)))
        .flatMapMany(Function.identity());
  }
}
