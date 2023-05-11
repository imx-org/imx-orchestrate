package org.dotwebstack.orchestrate.engine.fetch;

import static java.util.Collections.unmodifiableMap;
import static org.dotwebstack.orchestrate.engine.fetch.FetchUtils.cast;
import static org.dotwebstack.orchestrate.engine.fetch.FetchUtils.noopCombiner;
import static org.dotwebstack.orchestrate.engine.schema.SchemaConstants.HAS_LINEAGE_FIELD;

import graphql.schema.DataFetchingFieldSelectionSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Builder;
import org.dotwebstack.orchestrate.model.AbstractRelation;
import org.dotwebstack.orchestrate.model.Attribute;
import org.dotwebstack.orchestrate.model.ModelMapping;
import org.dotwebstack.orchestrate.model.ObjectType;
import org.dotwebstack.orchestrate.model.Path;
import org.dotwebstack.orchestrate.model.PropertyMapping;
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
          var propertyResult = mapProperty(objectResult, targetMapping.getPropertyMapping(property));

          if (!propertyResult.hasValue()) {
            return;
          }

          Object propertyValue = null;

          if (property instanceof Attribute attribute) {
            propertyValue = mapAttribute(attribute, propertyResult.getValue());
          }

          if (property instanceof AbstractRelation relation) {
            propertyValue = mapRelation(relation, propertyResult.getValue(), field.getSelectionSet());
          }

          if (propertyValue != null) {
            resultMap.put(property.getName(), propertyValue);

            lineageBuilder.orchestratedProperty(OrchestratedProperty.builder()
                .subject(ObjectReference.builder()
                    .objectType(targetType.getName())
                    .objectKey(objectResult.getKey())
                    .build())
                .property(property.getName())
                .value(propertyValue)
                .isDerivedFrom(propertyResult.getSourceProperties())
                .wasGeneratedBy(PropertyMappingExecution.builder()
                    .used(org.dotwebstack.orchestrate.model.lineage.PropertyMapping.builder()
                        .pathMapping(Set.of())
                        .build())
                    .build())
                .build());
          }
        });

    resultMap.put(HAS_LINEAGE_FIELD, lineageBuilder.build());
    return unmodifiableMap(resultMap);
  }

  private Object mapAttribute(Attribute attribute, Object value) {
    return attribute.getType()
        .mapSourceValue(value);
  }

  private Object mapRelation(AbstractRelation relation, Object value, DataFetchingFieldSelectionSet selectionSet) {
    var relType = modelMapping.getTargetType(relation.getTarget());

    if (value instanceof ObjectResult nestedObjectResult) {
      return map(nestedObjectResult, relType, selectionSet);
    }

    if (value instanceof CollectionResult nestedCollectionResult) {
      return nestedCollectionResult.getObjectResults()
          .stream()
          .map(nestedObjectResult -> map(nestedObjectResult, relType, selectionSet))
          .toList();
    }

    return null;
  }

  private PropertyResult mapProperty(ObjectResult objectResult, PropertyMapping propertyMapping) {
    return propertyMapping.getPathMappings()
        .stream()
        .reduce(PropertyResult.newResult(), (prevResult, pathMapping) -> {
          var pathResult = pathResult(objectResult, pathMapping.getPath());

          var mappedPathResult = pathMapping.getResultMappers()
              .stream()
              .reduce(pathResult, (acc, resultMapper) -> resultMapper.apply(acc), noopCombiner());

          return prevResult.withValue(mappedPathResult, SourceProperty.builder()
              .subject(ObjectReference.builder()
                  .objectType(objectResult.getType().getName())
                  .objectKey(objectResult.getKey())
                  .build())
              .property(pathMapping.getPath()
                  .getLastSegment())
              .propertyPath(pathMapping.getPath()
                  .getSegments())
              .value(pathResult)
              .build());
        }, noopCombiner());
  }

  private Object pathResult(ObjectResult objectResult, Path path) {
    if (path.isLeaf()) {
      return objectResult.getProperty(path.getFirstSegment());
    }

    var nestedResult = objectResult.getProperty(path.getFirstSegment());

    if (nestedResult == null) {
      return nestedResult;
    }

    var remainingPath = path.withoutFirstSegment();

    if (nestedResult instanceof ObjectResult nestedObjectResult) {
      return pathResult(nestedObjectResult, remainingPath);
    }

    if (nestedResult instanceof CollectionResult nestedCollectionResult) {
      List<ObjectResult> nestedObjectResults = nestedCollectionResult.getObjectResults()
          .stream()
          .map(nestedObjectResult -> pathResult(nestedObjectResult, remainingPath))
          .map(nestedPathResult -> cast(nestedPathResult, ObjectResult.class))
          .toList();

      return CollectionResult.builder()
          .objectResults(nestedObjectResults)
          .build();
    }

    return null;
  }
}
