package org.dotwebstack.orchestrate.engine.execution;

import lombok.Builder;
import org.dotwebstack.orchestrate.engine.fetch.FetchRequest;
import org.dotwebstack.orchestrate.model.ModelMapping;

@Builder(toBuilder = true)
public final class ExecutionPlanner {

  public ExecutionPlan plan(ModelMapping modelMapping, FetchRequest request) {
    return ExecutionPlan.builder()
        .modelMapping(modelMapping)
        .request(request)
        .build();
  }
}
