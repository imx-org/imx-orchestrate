package org.dotwebstack.orchestrate.model;

import java.util.Set;
import lombok.Builder;
import lombok.Getter;
import org.dotwebstack.orchestrate.model.lineage.SourceProperty;

@Getter
@Builder(toBuilder = true)
public final class PropertyResult {

  private final Object value;

  private final Set<SourceProperty> sourceProperties;

  public boolean isEmpty() {
    return value == null;
  }

  public boolean notEmpty() {
    return !isEmpty();
  }

  public static PropertyResult empty() {
    return PropertyResult.builder()
        .build();
  }
}
