package org.dotwebstack.orchestrate.engine.execution;

import lombok.Builder;
import lombok.Getter;
import org.dotwebstack.orchestrate.engine.fetch.FetchRequest;
import org.dotwebstack.orchestrate.engine.fetch.FetchResult;
import org.dotwebstack.orchestrate.model.ModelMapping;

@Getter
@Builder(toBuilder = true)
public final class ExecutionContext {

  private final ModelMapping modelMapping;

  private final FetchRequest request;

  private final FetchResult result;

  private final ExecutionContext previousContext;

  public ExecutionContext withResult(FetchResult result) {
    return toBuilder()
        .previousContext(this)
        .result(result)
        .build();
  }
}
