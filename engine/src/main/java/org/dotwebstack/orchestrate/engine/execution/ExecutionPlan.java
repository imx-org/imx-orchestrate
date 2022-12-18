package org.dotwebstack.orchestrate.engine.execution;

import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Singular;
import org.dotwebstack.orchestrate.engine.fetch.FetchRequest;
import org.dotwebstack.orchestrate.engine.fetch.FetchResult;
import org.dotwebstack.orchestrate.model.ModelMapping;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Builder(toBuilder = true)
public final class ExecutionPlan {

  private final ModelMapping modelMapping;

  private final FetchRequest request;

  @Singular
  private final List<ExecutionStage> stages = new ArrayList<>();

  public Mono<FetchResult> execute() {
    var initialContext = ExecutionContext.builder()
        .modelMapping(modelMapping)
        .request(request)
        .build();

    return Flux.fromIterable(stages)
        .reduce(Mono.just(initialContext), (acc, stage) -> acc.flatMap(context -> stage.execute(context)
            .map(context::withResult)))
        .flatMap(Mono::single)
        .map(ExecutionContext::getResult);
  }
}
