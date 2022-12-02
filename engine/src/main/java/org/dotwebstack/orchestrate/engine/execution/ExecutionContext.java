package org.dotwebstack.orchestrate.engine.execution;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

@Getter
@Builder(toBuilder = true, access = AccessLevel.PRIVATE)
public final class ExecutionContext {

  @NonNull
  private final ExecutionInput input;

  @NonNull
  private final ExecutionResult result;

  private final ExecutionContext previousContext;

  public ExecutionContext withResult(ExecutionResult result) {
    return toBuilder()
        .previousContext(this)
        .result(result)
        .build();
  }

  public static ExecutionContext create(ExecutionInput input) {
    return builder()
        .input(input)
        .result(ExecutionResult.create())
        .build();
  }
}
