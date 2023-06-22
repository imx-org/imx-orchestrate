package org.dotwebstack.orchestrate.model;

import lombok.Builder;
import lombok.Getter;
import org.dotwebstack.orchestrate.model.lineage.PathMapping;
import org.dotwebstack.orchestrate.model.lineage.SourceProperty;

@Getter
@Builder(toBuilder = true)
public class PathResult {

  private final Object value;

  private final SourceProperty sourceProperty;

  private final PathMapping pathMapping;

  public boolean isNull() {
    return value == null;
  }

  public boolean isNotNull() {
    return value != null;
  }

  public PathResult withValue(Object newValue) {
    return toBuilder()
        .value(newValue)
        .build();
  }

  public PathResult withPathMapping(PathMapping pathMapping) {
    return toBuilder()
        .pathMapping(pathMapping)
        .build();
  }

  public static PathResult empty() {
    return PathResult.builder()
        .build();
  }
}
