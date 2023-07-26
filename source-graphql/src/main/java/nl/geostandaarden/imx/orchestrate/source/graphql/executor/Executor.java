package nl.geostandaarden.imx.orchestrate.source.graphql.executor;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import reactor.core.publisher.Mono;

public interface Executor {

  Mono<ExecutionResult> execute(ExecutionInput input);
}
