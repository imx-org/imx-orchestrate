package org.dotwebstack.orchestrate.model;

import static java.util.stream.Collectors.toMap;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
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
    // TODO: Remove null-check once parser workaround has been resolved
    if (targetModel != null) {
      validateModel(targetModel);
      this.targetModel = resolveInverseRelations(Set.of(targetModel))
          .iterator()
          .next();
    } else {
      this.targetModel = targetModel;
    }

    sourceModels.forEach(this::validateModel);
    this.sourceModels = resolveInverseRelations(sourceModels);
    this.sourceModelMap = this.sourceModels.stream()
        .collect(toMap(Model::getAlias, Function.identity()));

    this.objectTypeMappings = objectTypeMappings;
    this.lineageNameMapping = Optional.ofNullable(lineageNameMapping)
        .orElse(Map.of());
  }

  private Set<Model> resolveInverseRelations(Set<Model> models) {
    var mutableSourceModelMap = models.stream()
        .collect(toMap(Model::getAlias, Function.identity()));

    models.forEach(model -> model.getObjectTypes()
        .forEach(objectType -> objectType.getProperties(Relation.class)
            .stream()
            .filter(relation -> relation.getInverseName() != null)
            .forEach(relation -> {
              var relTarget = relation.getTarget();

              var relTargetModel = Optional.ofNullable(relTarget.getModelAlias())
                  .or(() -> Optional.of(model.getAlias()))
                  .map(mutableSourceModelMap::get)
                  .orElseThrow(() -> new ModelException("Source model not found for relation target: " + relation));

              var inverseRelation = InverseRelation.builder()
                  .originRelation(relation)
                  .target(ObjectTypeRef.forType(model.getAlias(), objectType.getName()))
                  .build();

              mutableSourceModelMap.put(relTargetModel.getAlias(), relTargetModel.replaceObjectType(
                  relTargetModel.getObjectType(relTarget)
                      .appendProperty(inverseRelation)));
            })));

    return Set.copyOf(mutableSourceModelMap.values());
  }

  private void validateModel(Model model) {
    if (model.getAlias() == null) {
      throw new ModelException("Models must contain an alias.");
    }
  }

  public ObjectType getTargetType(ObjectTypeRef sourceTypeRef) {
    return targetModel.getObjectType(sourceTypeRef.getName());
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

  public ObjectTypeMapping getObjectTypeMapping(ObjectType objectType) {
    return getObjectTypeMapping(objectType.getName());
  }
}
