package org.dotwebstack.orchestrate.model;

import java.util.Map;
import java.util.Optional;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import lombok.extern.jackson.Jacksonized;

@Getter
@Jacksonized
@Builder(toBuilder = true)
public final class ObjectTypeMapping {

  private final SourceTypeRef sourceRoot;

  @Singular
  private final Map<String, FieldMapping> fieldMappings;

  public FieldMapping getFieldMapping(String name) {
    return Optional.ofNullable(fieldMappings.get(name))
        .orElseThrow(() -> new ModelException("Field mapping not found: " + name));
  }
}
