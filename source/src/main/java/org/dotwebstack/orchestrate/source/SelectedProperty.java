package org.dotwebstack.orchestrate.source;

import static java.util.Collections.emptyList;

import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.dotwebstack.orchestrate.model.Property;

@Getter
@EqualsAndHashCode
@RequiredArgsConstructor
public final class SelectedProperty {

  private final Property property;

  private final List<SelectedProperty> selectedProperties;

  public SelectedProperty(Property property) {
    this(property, emptyList());
  }

  @Override
  public String toString() {
    return property.getName()
        .concat(selectedProperties.isEmpty() ? "" : " " + selectedProperties);
  }
}
