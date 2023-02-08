package org.dotwebstack.orchestrate.model;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import lombok.extern.jackson.Jacksonized;

@Getter
@Jacksonized
@Builder(toBuilder = true)
public final class PropertyMapping {

  @Singular
  private final List<PropertyPathMapping> pathMappings;
}
