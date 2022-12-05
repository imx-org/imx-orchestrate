package org.dotwebstack.orchestrate.model.mapping;

import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

@Getter
@Builder(toBuilder = true)
public class ObjectTypeMapping {

  private final ObjectTypeRef sourceRoot;

  @Singular
  private final Map<String, FieldMapping> fieldMappings;
}
