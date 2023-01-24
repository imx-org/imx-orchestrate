package org.dotwebstack.orchestrate.model;

import java.util.Map;
import java.util.Optional;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

@Getter
@Builder(toBuilder = true)
public final class ModelMapping {

  private final Model targetModel;

  @Singular
  private final Map<String, Model> sourceModels;

  @Singular
  private final Map<String, ObjectTypeMapping> objectTypeMappings;

  public Optional<Model> getSourceModel(String key) {
    return Optional.ofNullable(sourceModels.get(key));
  }

  public Optional<ObjectTypeMapping> getObjectTypeMapping(String name) {
    return Optional.ofNullable(objectTypeMappings.get(name));
  }
}
