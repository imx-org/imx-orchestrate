package org.dotwebstack.orchestrate.engine.fetch;

import java.util.Map;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@Builder(toBuilder = true)
@EqualsAndHashCode
public final class FetchInput {

  private final FetchInput parent;

  private final Map<String, Object> data;

  public FetchInput withData(Map<String, Object> data) {
    return toBuilder()
        .parent(this)
        .data(data)
        .build();
  }
}
