package org.dotwebstack.orchestrate.engine.fetch;

import static java.util.Collections.unmodifiableSet;

import java.util.HashSet;
import java.util.Set;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.dotwebstack.orchestrate.model.lineage.SourceProperty;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class PropertyResult {

  private final Object value;

  private final Set<SourceProperty> sourceProperties;

  public boolean hasValue() {
    return value != null;
  }

  public PropertyResult withValue(Object newValue, SourceProperty sourceProperty) {
    var newSourceProperties = new HashSet<>(sourceProperties);
    newSourceProperties.add(sourceProperty);
    return new PropertyResult(newValue, unmodifiableSet(newSourceProperties));
  }

  public static PropertyResult newResult() {
    return new PropertyResult(null, Set.of());
  }
}
