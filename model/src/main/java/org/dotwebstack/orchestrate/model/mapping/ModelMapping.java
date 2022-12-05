package org.dotwebstack.orchestrate.model.mapping;

import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

@Getter
@Builder(toBuilder = true)
public class ModelMapping {

  @Singular
  private final Map<String, ObjectTypeMapping> objectTypeMappings;
}
