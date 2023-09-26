package nl.geostandaarden.imx.orchestrate.engine.exchange;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import nl.geostandaarden.imx.orchestrate.engine.OrchestrateException;
import nl.geostandaarden.imx.orchestrate.model.Property;
import nl.geostandaarden.imx.orchestrate.model.Relation;

@Getter
@EqualsAndHashCode
@Builder(toBuilder = true)
public final class SelectedProperty {

  private final Property property;

  private final DataRequest nestedRequest;

  public String getName() {
    return property.getName();
  }

  @Override
  public String toString() {
    return property.getName();
  }

  public static SelectedProperty forProperty(Property property) {
    if (property instanceof Relation) {
      throw new OrchestrateException("Using static constructor for Relation properties is not supported (yet).");
    }

    return SelectedProperty.builder()
        .property(property)
        .build();
  }
}
