package org.dotwebstack.orchestrate.engine.fetch;

import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableMap;
import static org.dotwebstack.orchestrate.engine.fetch.FetchUtils.cast;
import static org.dotwebstack.orchestrate.engine.schema.SchemaConstants.HAS_LINEAGE_FIELD;
import static org.dotwebstack.orchestrate.model.ModelUtils.noopCombiner;

import graphql.schema.DataFetchingFieldSelectionSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Builder;
import org.dotwebstack.orchestrate.engine.OrchestrateException;
import org.dotwebstack.orchestrate.model.AbstractRelation;
import org.dotwebstack.orchestrate.model.Attribute;
import org.dotwebstack.orchestrate.model.CollectionResult;
import org.dotwebstack.orchestrate.model.ModelMapping;
import org.dotwebstack.orchestrate.model.ObjectResult;
import org.dotwebstack.orchestrate.model.ObjectType;
import org.dotwebstack.orchestrate.model.Path;
import org.dotwebstack.orchestrate.model.PathMapping;
import org.dotwebstack.orchestrate.model.PathResult;
import org.dotwebstack.orchestrate.model.Property;
import org.dotwebstack.orchestrate.model.PropertyMapping;
import org.dotwebstack.orchestrate.model.PropertyResult;
import org.dotwebstack.orchestrate.model.combiners.CoalesceCombinerType;
import org.dotwebstack.orchestrate.model.combiners.NoopCombinerType;
import org.dotwebstack.orchestrate.model.lineage.ObjectLineage;
import org.dotwebstack.orchestrate.model.lineage.ObjectReference;
import org.dotwebstack.orchestrate.model.lineage.OrchestratedProperty;
import org.dotwebstack.orchestrate.model.lineage.PropertyMappingExecution;
import org.dotwebstack.orchestrate.model.lineage.SourceProperty;

@Builder
public final class ObjectResultMapper {

  private final ModelMapping modelMapping;

  public Map<String, Object> map(ObjectResult objectResult, ObjectType targetType,
      DataFetchingFieldSelectionSet selectionSet) {
    var targetMapping = modelMapping.getObjectTypeMapping(targetType);
    var resultMap = new HashMap<String, Object>();
    var lineageBuilder = ObjectLineage.builder();

    selectionSet.getImmediateFields()
        .forEach(field -> {
          if (!targetType.hasProperty(field.getName())) {
            return;
          }

          var property = targetType.getProperty(field.getName());
          var propertyResult = mapProperty(objectResult, property, targetMapping.getPropertyMapping(property));

          if (propertyResult == null) {
            return;
          }

          Object propertyValue = null;
          Object lineageValue = null;

          if (property instanceof Attribute attribute) {
            propertyValue = mapAttribute(attribute, propertyResult.getValue());

            if (propertyValue != null) {
              lineageValue = attribute.getType()
                  .mapLineageValue(propertyValue);
            }
          }

          if (property instanceof AbstractRelation relation) {
            propertyValue = mapRelation(relation, propertyResult.getValue(), field.getSelectionSet());
            lineageValue = propertyValue;
          }

          if (propertyValue != null) {
            resultMap.put(property.getName(), propertyValue);

            lineageBuilder.orchestratedProperty(OrchestratedProperty.builder()
                .subject(ObjectReference.builder()
                    .objectType(targetType.getName())
                    .objectKey(objectResult.getKey())
                    .build())
                .property(property.getName())
                .value(lineageValue)
                .isDerivedFrom(propertyResult.getSourceProperties())
                .wasGeneratedBy(PropertyMappingExecution.builder()
                    .used(propertyResult.getPropertyMapping())
                    .build())
                .build());
          }
        });

    resultMap.put(HAS_LINEAGE_FIELD, lineageBuilder.build());
    return unmodifiableMap(resultMap);
  }

  private Object mapAttribute(Attribute attribute, Object value) {
    if (value == null) {
      return null;
    }

    return attribute.getType()
        .mapSourceValue(value);
  }

  private Object mapRelation(AbstractRelation relation, Object value, DataFetchingFieldSelectionSet selectionSet) {
    var relType = modelMapping.getTargetType(relation.getTarget());

    if (value instanceof ObjectResult nestedObjectResult) {
      return map(nestedObjectResult, relType, selectionSet);
    }

    if (value instanceof List<?> nestedResultList) {
      return nestedResultList.stream()
          .map(nestedResult -> {
            if (nestedResult instanceof ObjectResult nestedObjectResult) {
              return map(nestedObjectResult, relType, selectionSet);
            }

            if (nestedResult instanceof Map<?, ?> nestedMapResult) {
              return cast(nestedMapResult);
            }

            throw new OrchestrateException("Could not map nested result");
          })
          .toList();
    }

    return null;
  }

  private PropertyResult mapProperty(ObjectResult objectResult, Property property, PropertyMapping propertyMapping) {
    var pathResults = propertyMapping.getPathMappings()
        .stream()
        .flatMap(pathMapping -> pathResult(objectResult, property, pathMapping)
            .map(pathResult -> pathResult.withPathMapping(org.dotwebstack.orchestrate.model.lineage.PathMapping.builder()
                .addPath(org.dotwebstack.orchestrate.model.lineage.Path.builder()
                    .startNode(ObjectReference.fromResult(objectResult))
                    .segments(pathMapping.getPath()
                        .getSegments())
                    .references(Optional.ofNullable(pathResult.getSourceProperty())
                        .map(Set::of)
                        .orElse(emptySet()))
                    .build())
                .build())))
        .toList();

    var combiner = propertyMapping.getCombiner();

    if (combiner == null) {
      var hasMultiCardinality = !property.getCardinality()
          .isSingular();

      combiner = hasMultiCardinality
          ? new NoopCombinerType().create(Map.of())
          : new CoalesceCombinerType().create(Map.of());
    }

    var propertyMappingExecution = org.dotwebstack.orchestrate.model.lineage.PropertyMapping.builder()
        .pathMapping(pathResults.stream()
            .map(PathResult::getPathMapping)
            .collect(Collectors.toSet()))
        .build();

    return combiner.apply(pathResults)
        .withPropertyMapping(propertyMappingExecution);
  }

  private Stream<PathResult> pathResult(ObjectResult objectResult, Property property, PathMapping pathMapping) {
    var pathResults = pathResult(objectResult, pathMapping.getPath(), pathMapping.getPath());

    return pathResults.flatMap(pathResult -> {
      // TODO: Lazy fetching & multi cardinality
      var nextedPathResult = pathMapping.getNextPathMappings()
          .stream()
          .flatMap(nextPathMapping -> {
            var ifMatch = nextPathMapping.getIfMatch();

            if (ifMatch != null && ifMatch.test(pathResult.getValue())) {
              return pathResult(objectResult, property, nextPathMapping);
            }

            return Stream.of(pathResult);
          })
          .findFirst()
          .orElse(pathResult);

      var mappedPathResult = pathMapping.getResultMappers()
          .stream()
          .reduce(nextedPathResult, (acc, resultMapper) -> resultMapper.apply(acc, property), noopCombiner());

      return Stream.of(mappedPathResult);
    });
  }

  private Stream<PathResult> pathResult(ObjectResult objectResult, Path path, Path fullPath) {
    var currentSegment = path.getFirstSegment();

    if (path.isLeaf()) {
      var propertyValue = objectResult.getProperty(currentSegment);

      var pathResult = PathResult.builder()
          .value(propertyValue)
          .sourceProperty(SourceProperty.builder()
              .subject(ObjectReference.fromResult(objectResult))
              .property(currentSegment)
              .value(propertyValue)
              .path(fullPath.getSegments())
              .build())
          .build();

      return Stream.of(pathResult);
    }

    var nestedResult = objectResult.getProperty(currentSegment);

    if (nestedResult == null) {
      return Stream.of(PathResult.empty());
    }

    var remainingPath = path.withoutFirstSegment();

    if (nestedResult instanceof ObjectResult nestedObjectResult) {
      return pathResult(nestedObjectResult, remainingPath, fullPath);
    }

    if (nestedResult instanceof CollectionResult nestedCollectionResult) {
      return nestedCollectionResult.getObjectResults()
          .stream()
          .flatMap(nestedObjectResult -> pathResult(nestedObjectResult, remainingPath, fullPath));
    }

    if (nestedResult instanceof Map<?, ?> mapResult) {
      return pathResult(objectResult.withProperties(cast(mapResult)), remainingPath, fullPath);
    }

    throw new OrchestrateException("Could not map path: " + path);
  }
}
