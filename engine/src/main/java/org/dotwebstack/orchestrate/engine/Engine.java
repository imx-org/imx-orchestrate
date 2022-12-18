package org.dotwebstack.orchestrate.engine;

import lombok.Builder;
import org.dotwebstack.orchestrate.engine.fetch.FetchRequest;
import org.dotwebstack.orchestrate.engine.execution.ExecutionPlanner;
import org.dotwebstack.orchestrate.engine.fetch.FetchResult;
import org.dotwebstack.orchestrate.model.ModelMapping;
import reactor.core.publisher.Mono;

@Builder(toBuilder = true)
public final class Engine {

  private final ModelMapping modelMapping;

  @Builder.Default
  private final ExecutionPlanner executionPlanner = ExecutionPlanner.builder()
      .build();

  public Mono<FetchResult> fetch(FetchRequest request) {
    return executionPlanner.plan(modelMapping, request)
        .execute();
  }
}
