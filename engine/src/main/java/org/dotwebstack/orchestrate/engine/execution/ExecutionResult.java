package org.dotwebstack.orchestrate.engine.execution;

import lombok.AccessLevel;
import lombok.Builder;

@Builder(toBuilder = true, access = AccessLevel.PRIVATE)
public final class ExecutionResult {

  public static ExecutionResult create() {
    return ExecutionResult.builder()
        .build();
  }
}
