package org.dotwebstack.orchestrate.engine.fetch;

import java.util.Map;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(toBuilder = true)
public final class FetchContext {

  private final FetchContext parent;

  private final Map<String, Object> input;

  public FetchContext withInput(Map<String, Object> input) {
    return toBuilder()
        .parent(this)
        .input(input)
        .build();
  }
}
