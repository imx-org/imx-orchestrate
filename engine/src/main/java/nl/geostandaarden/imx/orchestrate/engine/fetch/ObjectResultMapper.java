package nl.geostandaarden.imx.orchestrate.engine.fetch;

import static nl.geostandaarden.imx.orchestrate.engine.fetch.FetchUtils.cast;
import static nl.geostandaarden.imx.orchestrate.model.ModelUtils.noopCombiner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Builder;
import nl.geostandaarden.imx.orchestrate.engine.OrchestrateException;
import nl.geostandaarden.imx.orchestrate.engine.exchange.CollectionResult;
import nl.geostandaarden.imx.orchestrate.engine.exchange.DataRequest;
import nl.geostandaarden.imx.orchestrate.engine.exchange.ObjectResult;
import nl.geostandaarden.imx.orchestrate.model.AbstractRelation;
import nl.geostandaarden.imx.orchestrate.model.Attribute;
import nl.geostandaarden.imx.orchestrate.model.ModelMapping;
import nl.geostandaarden.imx.orchestrate.model.Path;
import nl.geostandaarden.imx.orchestrate.model.PathMapping;
import nl.geostandaarden.imx.orchestrate.model.Property;
import nl.geostandaarden.imx.orchestrate.model.PropertyMapping;
import nl.geostandaarden.imx.orchestrate.model.combiners.CoalesceCombinerType;
import nl.geostandaarden.imx.orchestrate.model.combiners.NoopCombinerType;
import nl.geostandaarden.imx.orchestrate.model.lineage.ObjectLineage;
import nl.geostandaarden.imx.orchestrate.model.lineage.ObjectReference;
import nl.geostandaarden.imx.orchestrate.model.lineage.OrchestratedDataElement;
import nl.geostandaarden.imx.orchestrate.model.lineage.PathExecution;
import nl.geostandaarden.imx.orchestrate.model.lineage.PathMappingExecution;
import nl.geostandaarden.imx.orchestrate.model.lineage.PropertyMappingExecution;
import nl.geostandaarden.imx.orchestrate.model.lineage.SourceDataElement;
import nl.geostandaarden.imx.orchestrate.model.result.PathMappingResult;
import nl.geostandaarden.imx.orchestrate.model.result.PathResult;
import nl.geostandaarden.imx.orchestrate.model.result.PropertyMappingResult;

@Builder
public final class ObjectResultMapper {

  private final ModelMapping modelMapping;

  public ObjectResult map(ObjectResult objectResult, DataRequest request) {
    var targetType = request.getObjectType();
    var targetMapping = modelMapping.getObjectTypeMapping(targetType);
    var properties = new HashMap<String, Object>();
    var lineageBuilder = ObjectLineage.builder();

    request.getSelectedProperties()
        .forEach(selectedProperty -> {
          if (!targetType.hasProperty(selectedProperty.getName())) {
            return;
          }

          var property = targetType.getProperty(selectedProperty.getName());
          var propertyMapping = targetMapping.getPropertyMapping(property);

          // TODO: Refactor
          if (!propertyMapping.isPresent()) {
            var propertyValue = map(objectResult, selectedProperty.getNestedRequest())
                .getProperties();
            properties.put(property.getName(), propertyValue);
            return;
          }

          var propertyResult = mapProperty(objectResult, property, propertyMapping.get());

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

          if (property instanceof AbstractRelation) {
            propertyValue = mapRelation(propertyResult.getValue(), selectedProperty.getNestedRequest());
            lineageValue = getRelationLineageValue(propertyValue);
          }

          if (propertyValue != null) {
            properties.put(property.getName(), propertyValue);

            List<?> lineageValues;
            if (lineageValue instanceof List<?> values) {
              lineageValues = values;
            } else {
              if (lineageValue == null) {
                lineageValues = List.of();
              } else {
                lineageValues = List.of(lineageValue);
              }
            }

            lineageValues.forEach(value -> lineageBuilder.orchestratedDataElement(OrchestratedDataElement.builder()
                .subject(ObjectReference.builder()
                    .objectType(targetType.getName())
                    .objectKey(objectResult.getKey())
                    .build())
                .property(property.getName())
                .value(value)
                .wasDerivedFrom(propertyResult.getSourceDataElements())
                .wasGeneratedBy(propertyResult.getPropertyMappingExecution())
                .build()));
          }
        });

    return ObjectResult.builder()
        .type(targetType)
        .properties(properties)
        .lineage(lineageBuilder.build())
        .build();
  }

  private Object getRelationLineageValue(Object relationValue) {
    List<?> values;

    if (relationValue instanceof List<?> relationValues) {
      values = relationValues;
    } else {
      if (relationValue == null) {
        values = List.of();
      } else {
        values = List.of(relationValue);
      }
    }

    return values.stream()
        .map(this::getSubjectReferenceFromRelationValue)
        .toList();
  }

  private ObjectReference getSubjectReferenceFromRelationValue(Object value) {
    if (value instanceof ObjectResult objectResult && (objectResult.getLineage() != null)) {
      return objectResult.getLineage()
          .getOrchestratedDataElements()
          .stream()
          .map(OrchestratedDataElement::getSubject)
          .findFirst()
          .orElseThrow(() -> new OrchestrateException(
              String.format("Expected data elements in relation value lineage but was %s", value)));
    }
    throw new OrchestrateException(String.format("Expected object result but was %s", value));
  }

  private Object mapAttribute(Attribute attribute, Object value) {
    if (value == null) {
      return null;
    }

    return attribute.getType()
        .mapSourceValue(value);
  }

  private Object mapRelation(Object value, DataRequest request) {
    if (value instanceof ObjectResult nestedObjectResult) {
      return map(nestedObjectResult, request);
    }

    if (value instanceof List<?> nestedResultList) {
      return nestedResultList.stream()
          .flatMap(nestedResult -> {
            if (nestedResult instanceof ObjectResult nestedObjectResult) {
              return Stream.of(map(nestedObjectResult, request));
            }

            if (nestedResult instanceof CollectionResult nestedCollectionResult) {
              return nestedCollectionResult.getObjectResults()
                  .stream()
                  .map(nestedObjectResult -> map(nestedObjectResult, request));
            }

            throw new OrchestrateException("Could not map nested result");
          })
          .toList();
    }

    return null;
  }

  private PropertyMappingResult mapProperty(ObjectResult objectResult, Property property, PropertyMapping propertyMapping) {
    var pathMappingResults = propertyMapping.getPathMappings()
        .stream()
        .map(pathMapping -> pathMappingResult(objectResult, property, pathMapping))
        .toList();

    var combiner = propertyMapping.getCombiner();

    if (combiner == null) {
      var hasMultiCardinality = !property.getCardinality()
          .isSingular();

      combiner = hasMultiCardinality
          ? new NoopCombinerType().create(Map.of())
          : new CoalesceCombinerType().create(Map.of());
    }

    PropertyMappingExecution propertyMappingExecution = PropertyMappingExecution.builder()
        .used(propertyMapping)
        .wasInformedBy(pathMappingResults.stream()
            .map(PathMappingResult::getPathMappingExecution)
            .toList())
        .build();

    var pathResults = pathMappingResults.stream()
        .map(PathMappingResult::getPathResults)
        .flatMap(List::stream)
        .toList();

    return combiner.apply(pathResults)
        .withPropertyMappingExecution(propertyMappingExecution);
  }

  private PathMappingResult pathMappingResult(ObjectResult objectResult, Property property, PathMapping pathMapping) {
    var pathResults = pathResult(objectResult, property, pathMapping.getPath(), pathMapping.getPath())
        .flatMap(pathResult -> resultMapPathResult(pathResult, objectResult, property, pathMapping))
        .toList();

    return PathMappingResult.builder()
        .pathResults(pathResults)
        .pathMappingExecution(PathMappingExecution.builder()
            .used(pathMapping)
            .wasInformedBy(pathResults.stream()
                .map(PathResult::getPathExecution)
                .toList())
            .build())
        .build();
  }

  private Object mapLineageValue(Object value, Property property) {
    if (property instanceof Attribute attribute) {
      return attribute.getType()
          .mapLineageValue(value);
    }

    return value;
  }

  private Stream<PathResult> pathResult(ObjectResult objectResult, Property property, Path path, Path fullPath) {
    var currentSegment = path.getFirstSegment();

    if (path.isLeaf()) {
      var propertyValue = objectResult.getProperty(currentSegment);

      List<?> sourceDataValues;

      if (propertyValue == null) {
        sourceDataValues = List.of();
      } else if (propertyValue instanceof List<?> propertyValues) {
        sourceDataValues = propertyValues;
      } else {
        sourceDataValues = List.of(propertyValue);
      }

      Set<SourceDataElement> sourceDataElements = sourceDataValues.stream()
          .map(value -> SourceDataElement.builder()
              .subject(objectResult.getObjectReference())
              .property(currentSegment)
              .value(value instanceof ObjectResult objectResultValue ? objectResultValue.getObjectReference()
                  : mapLineageValue(value, property))
              .build())
          .collect(Collectors.toUnmodifiableSet());

      var pathResult = PathResult.builder()
          .value(propertyValue)
          .pathExecution(PathExecution.builder()
              .used(path)
              .startNode(objectResult.getObjectReference())
              .references(sourceDataElements)
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
      return pathResult(nestedObjectResult, property, remainingPath, fullPath);
    }

    if (nestedResult instanceof CollectionResult nestedCollectionResult) {
      return nestedCollectionResult.getObjectResults()
          .stream()
          .flatMap(nestedObjectResult -> pathResult(nestedObjectResult, property, remainingPath, fullPath));
    }

    if (nestedResult instanceof Map<?, ?> mapResult) {
      return pathResult(objectResult.withProperties(cast(mapResult)), property, remainingPath, fullPath);
    }

    throw new OrchestrateException("Could not map path: " + path);
  }

  private Stream<PathResult> resultMapPathResult(PathResult pathResult, ObjectResult objectResult, Property property, PathMapping pathMapping) {
    // TODO: Lazy fetching & multi cardinality
    var nextedPathResult = pathMapping.getNextPathMappings()
        .stream()
        .flatMap(nextPathMapping -> nextPathResults(pathResult, objectResult, property, nextPathMapping))
        .findFirst()
        .orElse(pathResult);

    var mappedPathResult = pathMapping.getResultMappers()
        .stream()
        .reduce(nextedPathResult, (acc, resultMapper) -> resultMapper.apply(acc, property), noopCombiner());

    return Stream.of(mappedPathResult);

  }

  private Stream<PathResult> nextPathResults(PathResult previousPathResult, ObjectResult objectResult, Property property, PathMapping nextPathMapping) {
    var ifMatch = nextPathMapping.getIfMatch();

    if (ifMatch != null && ifMatch.test(previousPathResult.getValue())) {
      return pathMappingResult(objectResult, property, nextPathMapping)
          .getPathResults().stream();
    }

    return Stream.of(previousPathResult);
  }
}
