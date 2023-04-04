package org.dotwebstack.orchestrate.engine.fetch;

import java.util.List;
import java.util.Set;
import java.util.function.Function;
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

  public final Flux<ObjectResult> execute(FetchInput input) {
    return Flux.from(fetch(input))
        .transform(this::executeNextOperations);
  }

  public final Flux<ObjectResult> executeBatch(List<FetchInput> inputs) {
    return Flux.from(fetchBatch(inputs))
        .transform(this::executeNextOperations);
  }

  protected abstract Publisher<ObjectResult> fetch(FetchInput input);

  protected abstract Publisher<ObjectResult> fetchBatch(List<FetchInput> inputs);

  private Publisher<ObjectResult> executeNextOperations(Flux<ObjectResult> resultFlux) {
    if (nextOperations.isEmpty()) {
      return resultFlux;
    }

    return Flux.fromIterable(nextOperations)
        .reduce(resultFlux, (acc, nextOperation) -> acc.transform(nextOperation::apply))
        .flatMapMany(Function.identity());
  }
}
