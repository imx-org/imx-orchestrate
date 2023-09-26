package nl.geostandaarden.imx.orchestrate.engine.fetch;

import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableMap;
import static nl.geostandaarden.imx.orchestrate.engine.fetch.FetchUtils.cast;
import static nl.geostandaarden.imx.orchestrate.model.ModelUtils.noopCombiner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
import nl.geostandaarden.imx.orchestrate.model.lineage.OrchestratedProperty;
import nl.geostandaarden.imx.orchestrate.model.lineage.PropertyMappingExecution;
import nl.geostandaarden.imx.orchestrate.model.lineage.SourceProperty;
import nl.geostandaarden.imx.orchestrate.model.result.PathResult;
import nl.geostandaarden.imx.orchestrate.model.result.PropertyResult;

@Builder
public final class ObjectResultMapper {

  private final ModelMapping modelMapping;

  public Map<String, Object> map(ObjectResult objectResult, DataRequest request) {
    var targetType = request.getObjectType();
    var targetMapping = modelMapping.getObjectTypeMapping(targetType);
    var resultMap = new HashMap<String, Object>();
    var lineageBuilder = ObjectLineage.builder();

    request.getSelectedProperties()
        .forEach(selectedProperty -> {
          if (!targetType.hasProperty(selectedProperty.getName())) {
            return;
          }

          var property = targetType.getProperty(selectedProperty.getName());
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

          if (property instanceof AbstractRelation) {
            propertyValue = mapRelation(propertyResult.getValue(), selectedProperty.getNestedRequest());
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

    return unmodifiableMap(resultMap);
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
          .map(nestedResult -> {
            if (nestedResult instanceof ObjectResult nestedObjectResult) {
              return map(nestedObjectResult, request);
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
            .map(pathResult -> pathResult.withPathMapping(nl.geostandaarden.imx.orchestrate.model.lineage.PathMapping.builder()
                .addPath(nl.geostandaarden.imx.orchestrate.model.lineage.Path.builder()
                    .startNode(objectResult.getObjectReference())
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

    var propertyMappingExecution = nl.geostandaarden.imx.orchestrate.model.lineage.PropertyMapping.builder()
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
              .subject(objectResult.getObjectReference())
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
