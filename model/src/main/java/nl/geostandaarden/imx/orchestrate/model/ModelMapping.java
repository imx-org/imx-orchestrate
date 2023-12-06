package nl.geostandaarden.imx.orchestrate.model;

import static java.util.stream.Collectors.toMap;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import lombok.extern.jackson.Jacksonized;

@Getter
public final class ModelMapping {

  private final Model targetModel;

  private final Set<Model> sourceModels;

  private final Map<String, Model> sourceModelMap;

  private final Set<SourceRelation> sourceRelations;

  private final Map<String, List<ObjectTypeMapping>> objectTypeMappings;

  private final Map<String, String> lineageNameMapping;

  @Jacksonized
  @Builder(toBuilder = true)
  public ModelMapping(Model targetModel, @Singular Set<Model> sourceModels,
      @Singular Set<SourceRelation> sourceRelations, @Singular Map<String, List<ObjectTypeMapping>> objectTypeMappings,
      Map<String, String> lineageNameMapping) {
    // TODO: Remove null-check once parser workaround has been resolved
    if (targetModel != null) {
      this.targetModel = resolveInverseRelations(targetModel);
    } else {
      this.targetModel = targetModel;
    }

    sourceModels.forEach(this::validateSourceModel);
    this.sourceRelations = sourceRelations;
    this.sourceModels = resolveInverseRelations(addSourceRelations(sourceModels));
    this.sourceModelMap = this.sourceModels.stream()
        .collect(toMap(Model::getAlias, Function.identity()));

    this.objectTypeMappings = objectTypeMappings;
    this.lineageNameMapping = Optional.ofNullable(lineageNameMapping)
        .orElse(Map.of());
  }

  private Set<Model> addSourceRelations(Set<Model> models) {
    // TODO: Remove null-check once parser workaround has been resolved
    if (models.isEmpty()) {
      return models;
    }

    var mutableSourceModelMap = models.stream()
        .collect(toMap(Model::getAlias, Function.identity()));

    sourceRelations.forEach(sourceRelation -> {
      var sourceType = sourceRelation.getSourceType();
      var sourceModelAlias = sourceType.getModelAlias();
      var sourceModel = mutableSourceModelMap.get(sourceModelAlias);

      mutableSourceModelMap.put(sourceModelAlias, sourceModel.replaceObjectType(
          sourceModel.getObjectType(sourceType)
              .appendProperty(sourceRelation.getProperty())));
    });

    return Set.copyOf(mutableSourceModelMap.values());
  }

  private Model resolveInverseRelations(Model model) {
    return model.getObjectTypes()
        .stream()
        .flatMap(objectType -> objectType.getProperties(Relation.class)
            .stream()
            .flatMap(relation -> {
              if (relation.getInverseName() == null) {
                return Stream.empty();
              }

              var inverseRelation = InverseRelation.builder()
                  .originRelation(relation)
                  .target(ObjectTypeRef.forType(model.getAlias(), objectType.getName()))
                  .build();

              return Stream.of(Map.entry(relation.getTarget(), inverseRelation));
            }))
        .reduce(model, (acc, targetRelation) -> {
          var targetObjectType = acc.getObjectType(targetRelation.getKey())
              .appendProperty(targetRelation.getValue());

          return acc.replaceObjectType(targetObjectType);
        }, ModelUtils.noopCombiner());
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

  private void validateSourceModel(Model model) {
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

  public List<ObjectTypeMapping> getObjectTypeMappings(String name) {
    return Optional.ofNullable(objectTypeMappings.get(name))
        .orElseThrow(() -> new ModelException("Object type mapping not found: " + name));
  }

  public List<ObjectTypeMapping> getObjectTypeMappings(ObjectType targetType) {
    return getObjectTypeMappings(targetType.getName());
  }

  public Optional<ObjectTypeMapping> getObjectTypeMapping(ObjectType targetType, ObjectType sourceRoot) {
    return getObjectTypeMappings(targetType)
        .stream()
        .filter(typeMapping -> typeMapping.getSourceRoot()
            .getName()
            .equals(sourceRoot.getName()))
        .findFirst();
  }
}
