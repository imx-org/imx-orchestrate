package nl.geostandaarden.imx.orchestrate.model.result;

import java.util.Set;
import lombok.Builder;
import lombok.Getter;
import nl.geostandaarden.imx.orchestrate.model.lineage.PropertyMapping;
import nl.geostandaarden.imx.orchestrate.model.lineage.SourceProperty;

@Getter
@Builder(toBuilder = true)
public final class PropertyResult {

  private final Object value;

  private final Set<SourceProperty> sourceProperties;

  private final nl.geostandaarden.imx.orchestrate.model.lineage.PropertyMapping propertyMapping;

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
