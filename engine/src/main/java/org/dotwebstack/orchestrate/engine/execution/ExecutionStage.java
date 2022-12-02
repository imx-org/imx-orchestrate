package org.dotwebstack.orchestrate.engine.execution;

import reactor.core.publisher.Mono;

@FunctionalInterface
public interface ExecutionStage {

  Mono<ExecutionResult> execute(ExecutionContext context);
}
