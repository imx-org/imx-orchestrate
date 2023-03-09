package org.dotwebstack.orchestrate.engine.fetch;

import java.util.Map;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
@Builder(toBuilder = true)
public final class FetchInput {

  private final Map<String, Object> data;

  public static FetchInput newInput(Map<String, Object> data) {
    return builder()
        .data(data)
        .build();
  }
}
