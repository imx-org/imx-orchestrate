package org.dotwebstack.orchestrate.model;

import lombok.Builder;
import lombok.Getter;
import org.dotwebstack.orchestrate.model.lineage.SourceProperty;

@Getter
@Builder(toBuilder = true)
public class PathResult {

  private final Object value;

  private final SourceProperty sourceProperty;

  public boolean isEmpty() {
    return value == null;
  }

  public boolean notEmpty() {
    return !isEmpty();
  }

  public PathResult withValue(Object newValue) {
    return toBuilder()
        .value(newValue)
        .build();
  }

  public static PathResult empty() {
    return PathResult.builder()
        .build();
  }
}
