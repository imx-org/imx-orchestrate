package nl.geostandaarden.imx.orchestrate.model.result;

import lombok.Builder;
import lombok.Getter;
import nl.geostandaarden.imx.orchestrate.model.lineage.PathExecution;

@Getter
@Builder(toBuilder = true)
public class PathResult {

  private final Object value;

  private final PathExecution pathExecution;

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

  public PathResult withPathExecution(PathExecution pathExecution) {
    return toBuilder()
        .pathExecution(pathExecution)
        .build();
  }

  public static PathResult empty() {
    return PathResult.builder()
        .build();
  }
}
