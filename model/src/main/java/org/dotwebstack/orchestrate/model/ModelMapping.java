package org.dotwebstack.orchestrate.model;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import lombok.extern.jackson.Jacksonized;

@Getter
public final class ModelMapping {

  private final Model targetModel;

  private final Set<Model> sourceModels;

  private final Map<String, Model> sourceModelMap;

  private final Map<String, ObjectTypeMapping> objectTypeMappings;

  private final Map<String, String> lineageNameMapping;

  @Jacksonized
  @Builder(toBuilder = true)
  public ModelMapping(Model targetModel, @Singular Set<Model> sourceModels,
      @Singular Map<String, ObjectTypeMapping> objectTypeMappings, Map<String, String> lineageNameMapping) {
    this.targetModel = targetModel;

    sourceModels.forEach(this::validateSourceModel);
    this.sourceModels = sourceModels;
    this.sourceModelMap = sourceModels.stream()
        .collect(Collectors.toMap(Model::getAlias, Function.identity()));

    this.objectTypeMappings = objectTypeMappings;
    this.lineageNameMapping = Optional.ofNullable(lineageNameMapping)
        .orElse(Map.of());
  }

  private void validateSourceModel(Model model) {
    if (model.getAlias() == null) {
      throw new ModelException("Source models must contain an alias.");
    }
  }

  public Model getSourceModel(String alias) {
    return Optional.ofNullable(sourceModelMap.get(alias))
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
