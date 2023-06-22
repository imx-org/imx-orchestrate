package org.dotwebstack.orchestrate.model;

import java.util.Set;
import lombok.Builder;
import lombok.Getter;
import org.dotwebstack.orchestrate.model.lineage.PropertyMapping;
import org.dotwebstack.orchestrate.model.lineage.SourceProperty;

@Getter
@Builder(toBuilder = true)
public final class PropertyResult {

  private final Object value;

  private final Set<SourceProperty> sourceProperties;

  private final PropertyMapping propertyMapping;

  public boolean isEmpty() {
    return value == null;
  }

  public PropertyResult withPropertyMapping(PropertyMapping propertyMapping) {
    return toBuilder()
        .propertyMapping(propertyMapping)
        .build();
  }

  public static PropertyResult empty() {
    return PropertyResult.builder()
        .build();
  }
}
