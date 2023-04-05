package org.dotwebstack.orchestrate.model;

import java.util.HashMap;
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

  @Builder.Default
  private final Map<String, String> lineageNameMapping = new HashMap<>();

  public Model getSourceModel(String alias) {
    return Optional.ofNullable(sourceModels.get(alias))
        .orElseThrow(() -> new ModelException("Source model not found: " + alias));
  }

  public ObjectType getSourceType(ObjectTypeRef sourceTypeRef) {
    if (sourceTypeRef.getModelAlias() == null) {
      throw new ModelException("Source type reference does not contain a model alias.");
    }

    return getSourceModel(sourceTypeRef.getModelAlias())
        .getObjectType(sourceTypeRef.getName());
  }

  public ObjectTypeMapping getObjectTypeMapping(String name) {
    return Optional.ofNullable(objectTypeMappings.get(name))
        .orElseThrow(() -> new ModelException("Object type mapping not found: " + name));
  }
}
