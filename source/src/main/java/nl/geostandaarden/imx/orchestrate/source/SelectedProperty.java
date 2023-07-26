package nl.geostandaarden.imx.orchestrate.source;

import static java.util.Collections.emptySet;

import java.util.Set;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import nl.geostandaarden.imx.orchestrate.model.Property;

@Getter
@EqualsAndHashCode
@RequiredArgsConstructor
public final class SelectedProperty {

  private final Property property;

  private final Set<SelectedProperty> selectedProperties;

  public SelectedProperty(Property property) {
    this(property, emptySet());
  }

  @Override
  public String toString() {
    return property.getName()
        .concat(selectedProperties.isEmpty() ? "" : " " + selectedProperties);
  }
}
