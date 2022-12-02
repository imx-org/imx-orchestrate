package org.dotwebstack.orchestrate.engine;

import org.dotwebstack.orchestrate.engine.execution.ExecutionInput;
import org.dotwebstack.orchestrate.engine.execution.ExecutionPlan;
import org.dotwebstack.orchestrate.engine.execution.ExecutionResult;
import reactor.core.publisher.Mono;

public final class Engine {

  public Mono<ExecutionResult> fetch() {
    var plan = new ExecutionPlan();
    var input = new ExecutionInput();

    return plan.execute(input);
  }
}
