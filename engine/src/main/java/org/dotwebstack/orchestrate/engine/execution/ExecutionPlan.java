package org.dotwebstack.orchestrate.engine.execution;

import java.util.ArrayList;
import java.util.List;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public final class ExecutionPlan {

  private final List<ExecutionStage> stages = new ArrayList<>();

  public Mono<ExecutionResult> execute(ExecutionInput input) {
    var initialContext = Mono.just(ExecutionContext.create(input));

    return Flux.fromIterable(stages)
        .reduce(initialContext, (acc, stage) -> acc.flatMap(context -> stage.execute(context)
            .map(context::withResult)))
        .flatMap(Mono::single)
        .map(ExecutionContext::getResult);
  }
}
