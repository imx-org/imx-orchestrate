package org.dotwebstack.orchestrate.engine.execution;

import org.dotwebstack.orchestrate.engine.fetch.FetchResult;
import reactor.core.publisher.Mono;

@FunctionalInterface
public interface ExecutionStage {

  Mono<FetchResult> execute(ExecutionContext context);
}
