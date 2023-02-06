package org.dotwebstack.orchestrate.model;

import java.util.Map;
import java.util.Optional;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

@Getter
@Builder(toBuilder = true)
public final class ObjectTypeMapping {

  private final SourceTypeRef sourceRoot;

  @Singular
  private final Map<String, PropertyMapping> propertyMappings;

  public PropertyMapping getPropertyMapping(String name) {
    return Optional.ofNullable(propertyMappings.get(name))
        .orElseThrow(() -> new ModelException("Attribute mapping not found: " + name));
  }
}
