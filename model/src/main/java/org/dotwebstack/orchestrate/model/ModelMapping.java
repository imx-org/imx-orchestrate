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
public final class ModelMapping {

  private final Model targetModel;

  @Singular
  private final Map<String, Model> sourceModels;

  @Singular
  private final Map<String, ObjectTypeMapping> objectTypeMappings;

  public Model getSourceModel(String key) {
    return Optional.ofNullable(sourceModels.get(key))
        .orElseThrow(() -> new ModelException("Source model not found: " + key));
  }

  public ObjectTypeMapping getObjectTypeMapping(String name) {
    return Optional.ofNullable(objectTypeMappings.get(name))
        .orElseThrow(() -> new ModelException("Object type mapping not found: " + name));
  }
}
