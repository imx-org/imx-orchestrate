package org.dotwebstack.orchestrate.engine.fetch;

import static java.util.Collections.unmodifiableMap;
import static java.util.stream.Collectors.toUnmodifiableSet;
import static org.dotwebstack.orchestrate.engine.fetch.FetchUtils.keyExtractor;
import static org.dotwebstack.orchestrate.engine.fetch.FetchUtils.noopCombiner;
import static org.dotwebstack.orchestrate.engine.fetch.FetchUtils.pathResult;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Builder;
import org.dotwebstack.orchestrate.engine.OrchestrateException;
import org.dotwebstack.orchestrate.model.Attribute;
import org.dotwebstack.orchestrate.model.ObjectType;
import org.dotwebstack.orchestrate.model.Property;
import org.dotwebstack.orchestrate.model.PropertyMapping;
import org.dotwebstack.orchestrate.model.PropertyPathMapping;
import org.dotwebstack.orchestrate.model.Relation;
import org.dotwebstack.orchestrate.model.lineage.ObjectLineage;
import org.dotwebstack.orchestrate.model.lineage.ObjectReference;
import org.dotwebstack.orchestrate.model.lineage.OrchestratedProperty;
import org.dotwebstack.orchestrate.model.lineage.PropertyMappingExecution;
import org.dotwebstack.orchestrate.model.lineage.PropertyPath;
import org.dotwebstack.orchestrate.model.lineage.PropertyPathMapping.PropertyPathMappingBuilder;
import org.dotwebstack.orchestrate.model.lineage.SourceProperty;
import org.dotwebstack.orchestrate.model.transforms.Transform;

@Builder
class ObjectResultMapper implements UnaryOperator<ObjectResult> {

  private final ObjectType targetType;

  private final Map<Property, PropertyMapping> propertyMappings;

  public ObjectResult apply(ObjectResult objectResult) {
    var targetReference = ObjectReference.builder()
        .objectType(targetType.getName())
        .objectKey(keyExtractor(objectResult.getType()).apply(objectResult))
        .build();

    var objectLineageBuilder = ObjectLineage.builder();

    Map<String, Object> resultData = propertyMappings.entrySet()
        .stream()
        .collect(HashMap::new, (acc, entry) -> acc.put(entry.getKey().getName(), mapPropertyResult(entry.getKey(),
            entry.getValue(), objectResult, targetReference, objectLineageBuilder)), HashMap::putAll);

    return ObjectResult.builder()
        .type(targetType)
        .properties(unmodifiableMap(resultData))
        .lineage(objectLineageBuilder.build())
        .build();
  }

  private Object mapPropertyResult(Property property, PropertyMapping propertyMapping, ObjectResult objectResult,
      ObjectReference targetReference, ObjectLineage.ObjectLineageBuilder objectLineageBuilder) {
    var sourceProperties = new LinkedHashSet<SourceProperty>();
    var propertyMappingPaths = new LinkedHashMap<PropertyPathMapping, PropertyPathMappingBuilder>();

    var resultValue = propertyMapping.getPathMappings()
        .stream()
        .reduce(null, (previousValue, pathMapping) -> {
          var pathValue = pathMapping.getPaths()
              .stream()
              .flatMap(path -> {
                // lineage
                PropertyPathMappingBuilder propertyPathMappingBuilder;
                if (propertyMappingPaths.containsKey(pathMapping)) {
                  propertyPathMappingBuilder = propertyMappingPaths.get(pathMapping);
                } else {
                  propertyPathMappingBuilder = org.dotwebstack.orchestrate.model.lineage.PropertyPathMapping.builder();
                  propertyMappingPaths.put(pathMapping, propertyPathMappingBuilder);
                }

                var pathBuilderForLineage = PropertyPath.builder();
                pathBuilderForLineage.segments(path.getSegments())
                    .startNode(ObjectReference.builder()
                        .objectType(objectResult.getType().getName())
                        .objectKey(objectResult.getProperties()
                            .entrySet()
                            .stream()
                            .filter(entry -> objectResult.getType()
                                .getIdentityProperties()
                                .stream()
                                .map(Property::getName)
                                .toList()
                                .contains(entry.getKey()))
                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)))
                        .build());
                // -------

                var pathResult = pathResult(objectResult, path);

                if (pathResult == null) {
                  propertyPathMappingBuilder.addPath(pathBuilderForLineage.build());
                  return Stream.empty();
                }

                if (pathResult instanceof CollectionResult collectionResult) {
                  if (property instanceof Relation) {
                    // TODO: Refactor
                    var propertyName = pathMapping.getPaths()
                        .get(0)
                        .getLastSegment();
                    ;

                    var relationSourceProperties = collectionResult.getObjectResults()
                        .stream()
                        .map(objResult -> {

                          var resultType = objResult.getType();

                          return SourceProperty.builder()
                              .subject(ObjectReference.builder()
                                  .objectType(resultType.getName())
                                  .objectKey(keyExtractor(resultType).apply(objResult))
                                  .build())
                              .property(propertyName)
                              .propertyPath(path.getSegments())
                              .value(objResult.getProperty(propertyName))
                              .build();
                        })
                        .collect(toUnmodifiableSet());

                    sourceProperties.addAll(relationSourceProperties);

                    propertyPathMappingBuilder.addPath(pathBuilderForLineage.references(relationSourceProperties)
                        .build());

                    return Stream.of(collectionResult.getObjectResults()
                        .stream()
                        .map(result -> result.getProperty(propertyName))
                        .toList());
                  }

                  throw new OrchestrateException("Could not map collection result.");
                }

                if (pathResult instanceof ObjectResult pathObjectResult) {
                  var value = pathObjectResult.getProperty(path.getLastSegment());

                  if (value == null) {
                    propertyPathMappingBuilder.addPath(pathBuilderForLineage.build());
                    return Stream.empty();
                  }

                  if (property instanceof Attribute attribute) {
                    value = attribute.getType()
                        .mapSourceValue(value);
                  }

                  var resultType = pathObjectResult.getType();

                  var sourceProperty = SourceProperty.builder()
                      .subject(ObjectReference.builder()
                          .objectType(resultType.getName())
                          .objectKey(keyExtractor(resultType).apply(pathObjectResult))
                          .build())
                      .property(path.getLastSegment())
                      .propertyPath(path.getSegments())
                      .value(value)
                      .build();

                  sourceProperties.add(sourceProperty);

                  propertyPathMappingBuilder.addPath(pathBuilderForLineage.references(Set.of(sourceProperty))
                      .build());

                  return Stream.of(value);
                }

                throw new OrchestrateException("Could not map result.");
              })
              .findFirst()
              .orElse(null);

          if (pathMapping.hasTransforms()) {
            pathValue = transform(pathValue, pathMapping.getTransforms());
          }

          if (pathMapping.hasCombiner()) {
            pathValue = pathMapping.getCombiner()
                .apply(pathValue, previousValue);
          }

          return pathValue;
        }, noopCombiner());

    if (resultValue == null) {
      return null;
    }

    if (resultValue instanceof List<?> resultList) {
      resultList.forEach(result -> {
        var orchestratedProperty = OrchestratedProperty.builder()
            .subject(targetReference)
            .property(property.getName())
            .value(result)
            .isDerivedFrom(sourceProperties)
            .wasGeneratedBy(PropertyMappingExecution.builder()
                .used(
                    org.dotwebstack.orchestrate.model.lineage.PropertyMapping.builder()
                        .pathMapping(propertyMappingPaths.values()
                            .stream()
                            .map(PropertyPathMappingBuilder::build)
                            .collect(Collectors.toSet()))
                        .build())
                .build())
            .build();

        objectLineageBuilder.orchestratedProperty(orchestratedProperty);
      });
    }

    return resultValue;
  }

  private Object transform(Object value, List<Transform> transforms) {
    return transforms.stream()
        .reduce(value, (acc, transform) -> transform.apply(acc), noopCombiner());
  }
}
