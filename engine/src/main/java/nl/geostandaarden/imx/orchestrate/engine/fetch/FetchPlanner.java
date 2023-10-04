package nl.geostandaarden.imx.orchestrate.engine.fetch;

import static java.util.Collections.unmodifiableSet;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toSet;
import static nl.geostandaarden.imx.orchestrate.model.ModelUtils.extractKey;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import nl.geostandaarden.imx.orchestrate.engine.OrchestrateException;
import nl.geostandaarden.imx.orchestrate.engine.exchange.CollectionRequest;
import nl.geostandaarden.imx.orchestrate.engine.exchange.CollectionResult;
import nl.geostandaarden.imx.orchestrate.engine.exchange.DataRequest;
import nl.geostandaarden.imx.orchestrate.engine.exchange.ObjectRequest;
import nl.geostandaarden.imx.orchestrate.engine.exchange.ObjectResult;
import nl.geostandaarden.imx.orchestrate.engine.exchange.SelectedProperty;
import nl.geostandaarden.imx.orchestrate.engine.source.Source;
import nl.geostandaarden.imx.orchestrate.model.AbstractRelation;
import nl.geostandaarden.imx.orchestrate.model.Attribute;
import nl.geostandaarden.imx.orchestrate.model.InverseRelation;
import nl.geostandaarden.imx.orchestrate.model.ModelMapping;
import nl.geostandaarden.imx.orchestrate.model.ObjectType;
import nl.geostandaarden.imx.orchestrate.model.ObjectTypeMapping;
import nl.geostandaarden.imx.orchestrate.model.ObjectTypeRef;
import nl.geostandaarden.imx.orchestrate.model.Path;
import nl.geostandaarden.imx.orchestrate.model.PathMapping;
import nl.geostandaarden.imx.orchestrate.model.Property;
import nl.geostandaarden.imx.orchestrate.model.Relation;
import nl.geostandaarden.imx.orchestrate.model.filters.FilterDefinition;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public final class FetchPlanner {

  private final ModelMapping modelMapping;

  private final Map<String, Source> sources;

  public Mono<ObjectResult> fetch(ObjectRequest request) {
    var typeMapping = modelMapping.getObjectTypeMapping(request.getObjectType());
    var sourcePaths = resolveSourcePaths(request, typeMapping, Path.fromProperties());

    var resultMapper = ObjectResultMapper.builder()
        .modelMapping(modelMapping)
        .build();

    var input = FetchInput.newInput(request.getObjectKey());

    return fetchSourceObject(typeMapping.getSourceRoot(), sourcePaths, false, null)
        .execute(input)
        .singleOrEmpty()
        .map(result -> ObjectResult.builder()
            .type(request.getObjectType())
            .properties(resultMapper.map(result, request))
            .build());
  }

  public Mono<CollectionResult> fetch(CollectionRequest request) {
    var typeMapping = modelMapping.getObjectTypeMapping(request.getObjectType());
    var sourcePaths = resolveSourcePaths(request, typeMapping, Path.fromProperties());

    var resultMapper = ObjectResultMapper.builder()
        .modelMapping(modelMapping)
        .build();

    var input = FetchInput.newInput(Map.of());

    return fetchSourceObject(typeMapping.getSourceRoot(), sourcePaths, true, null)
        .execute(input)
        .map(result -> ObjectResult.builder()
            .type(request.getObjectType())
            .properties(resultMapper.map(result, request))
            .build())
        .collectList()
        .map(objectResults -> CollectionResult.builder()
            .objectResults(objectResults)
            .build());
  }

  public Set<Path> resolveSourcePaths(DataRequest request, ObjectTypeMapping typeMapping, Path basePath) {
    return request.getSelectedProperties()
        .stream()
        .flatMap(selectedProperty -> {
          var property = selectedProperty.getProperty();
          var propertyMapping = typeMapping.getPropertyMapping(property);

          var sourcePaths = propertyMapping.getPathMappings()
              .stream()
              .flatMap(pathMapping -> {
                // TODO: Conditionality & recursion
                var nextPaths = pathMapping.getNextPathMappings()
                    .stream()
                    .map(PathMapping::getPath);

                return Stream.concat(Stream.of(pathMapping.getPath()), nextPaths)
                    .map(basePath::append);
              });

          if (property instanceof AbstractRelation) {
            var nestedRequest = Optional.ofNullable(selectedProperty.getNestedRequest())
                .orElseThrow(() -> new OrchestrateException("Nested request not present for relation: " + property.getName()));
            var nestedTypeMapping = modelMapping.getObjectTypeMapping(nestedRequest.getObjectType());

            return sourcePaths.flatMap(sourcePath -> resolveSourcePaths(nestedRequest, nestedTypeMapping, sourcePath).stream());
          }

          return sourcePaths;
        })
        .collect(toSet());
  }

  private FetchOperation fetchSourceObject(ObjectTypeRef sourceTypeRef, Set<Path> sourcePaths,
                                           boolean isCollection, FilterDefinition filter) {
    var source = sources.get(sourceTypeRef.getModelAlias());
    var sourceType = modelMapping.getSourceType(sourceTypeRef);
    var selectedProperties = new HashSet<>(selectIdentity(sourceTypeRef));

    sourcePaths.stream()
        .filter(Path::isLeaf)
        .map(sourcePath -> sourceType.getProperty(sourcePath.getFirstSegment()))
        .filter(not(Property::isIdentifier))
        .map(SelectedProperty::forProperty)
        .forEach(selectedProperties::add);

    var nextOperations = new HashSet<NextOperation>();

    sourcePaths.stream()
        .filter(not(Path::isLeaf))
        .collect(groupingBy(Path::getFirstSegment, mapping(Path::withoutFirstSegment, toSet())))
        .forEach((propertyName, nestedSourcePaths) -> {
          var property = sourceType.getProperty(propertyName);

          if (property instanceof InverseRelation inverseRelation) {
            var filterDefinition = createFilterDefinition(sourceType, inverseRelation);
            var originTypeRef = inverseRelation.getTarget(sourceTypeRef);

            nextOperations.add(NextOperation.builder()
                .property(inverseRelation)
                .delegateOperation(fetchSourceObject(originTypeRef, nestedSourcePaths, true, filterDefinition))
                .build());

            return;
          }

          if (property instanceof Relation relation) {
            var targetTypeRef = relation.getTarget(sourceTypeRef);
            var targetType = modelMapping.getSourceType(targetTypeRef);
            var filterMappings = relation.getFilterMappings();

            if (!filterMappings.isEmpty()) {
              var filterMapping = filterMappings.get(0);
              var sourcePath = filterMapping.getSourcePath();

              if (!sourcePath.isLeaf()) {
                throw new OrchestrateException("Only leaf paths are (currently) supported for filter mapping: " + filterMapping.getProperty());
              }

              var filterProperty = sourceType.getProperty(sourcePath.getFirstSegment());
              selectedProperties.add(SelectedProperty.forProperty(filterProperty));

              var targetProperty = targetType.getProperty(filterMapping.getProperty());

              if (targetProperty instanceof Attribute targetAttribute) {
                var relationFilter = FilterDefinition.builder()
                    .path(Path.fromProperties(targetProperty))
                    .operator(filterMapping.getOperator())
                    .valueExtractor(properties -> targetAttribute.getType()
                        .mapSourceValue(properties.get(sourcePath.getFirstSegment())))
                    .build();

                nextOperations.add(NextOperation.builder()
                    .property(relation)
                    .delegateOperation(fetchSourceObject(targetTypeRef, nestedSourcePaths, true, relationFilter))
                    .build());
              } else {
                throw new OrchestrateException("Filter property is not an attribute: " + targetProperty.getName());
              }

              return;
            }

            var keyMapping = relation.getKeyMapping();

            if (keyMapping != null) {
              keyMapping.values()
                  .forEach(keyPath -> {
                    if (!keyPath.isLeaf()) {
                      throw new OrchestrateException("Only leaf paths are (currently) supported for key mapping: " + keyPath);
                    }

                    var keyProperty = sourceType.getProperty(keyPath.getFirstSegment());
                    selectedProperties.add(SelectedProperty.forProperty(keyProperty));
                  });
            } else {
              var targetModel = modelMapping.getSourceModel(targetTypeRef.getModelAlias());

              // TODO: Refactor
              if (property.getCardinality().isSingular()) {
                selectedProperties.add(SelectedProperty.builder()
                    .property(property)
                    .nestedRequest(ObjectRequest.builder(targetModel)
                        .objectType(targetTypeRef)
                        .objectKey(Map.of())
                        .selectedProperties(selectIdentity(targetTypeRef))
                        .build())
                    .build());
              } else {
                // TODO: Filter
                selectedProperties.add(SelectedProperty.builder()
                    .property(property)
                    .nestedRequest(CollectionRequest.builder(targetModel)
                        .objectType(targetTypeRef)
                        .selectedProperties(selectIdentity(targetTypeRef))
                        .build())
                    .build());
              }
            }

            var identityPaths = targetType.getIdentityProperties()
                .stream()
                .map(Path::fromProperties)
                .collect(Collectors.toSet());

            // If only identity is selected, no next operation is needed
            if (identityPaths.equals(nestedSourcePaths)) {
              return;
            }

            nextOperations.add(NextOperation.builder()
                .property(relation)
                .delegateOperation(fetchSourceObject(targetTypeRef, nestedSourcePaths, false, null))
                .build());

            return;
          }

          throw new OrchestrateException("Could not map property: " + propertyName);
        });

    // TODO: Refactor
    var sourceModel = modelMapping.getSourceModel(sourceTypeRef.getModelAlias());

    if (isCollection) {
      return CollectionFetchOperation.builder()
          .model(sourceModel)
          .source(source)
          .objectType(sourceType)
          .selectedProperties(unmodifiableSet(selectedProperties))
          .nextOperations(unmodifiableSet(nextOperations))
          .filter(filter)
          .build();
    }

    return ObjectFetchOperation.builder()
        .model(sourceModel)
        .source(source)
        .objectType(sourceType)
        .selectedProperties(unmodifiableSet(selectedProperties))
        .nextOperations(unmodifiableSet(nextOperations))
        .build();
  }

  private FilterDefinition createFilterDefinition(ObjectType targetType, Map<String, Object> arguments) {
    if (arguments == null) {
      return null;
    }

    if (arguments.entrySet().size() > 1) {
      throw new OrchestrateException("Currently only a single filter property is supported.");
    }

    var firstEntry = arguments.entrySet()
        .iterator()
        .next();

    var pathMappings = modelMapping.getObjectTypeMapping(targetType)
        .getPropertyMapping(firstEntry.getKey())
        .getPathMappings();

    if (pathMappings.size() > 1) {
      throw new OrchestrateException("Currently only a single path mapping is supported when filtering.");
    }

    var firstPath = pathMappings.get(0)
        .getPath();

    if (!firstPath.isLeaf()) {
      throw new OrchestrateException("Currently only direct source root properties can be filtered.");
    }

    return ((Attribute) targetType.getProperty(firstEntry.getKey()))
        .getType()
        .createFilterDefinition(firstPath, firstEntry.getValue());
  }

  private FilterDefinition createFilterDefinition(ObjectType sourceType, InverseRelation inverseRelation) {
    var filterDefinition = FilterDefinition.builder();
    var keyMapping = inverseRelation.getOriginRelation()
        .getKeyMapping();

    if (keyMapping != null) {
      // TODO: Composite keys
      var keyMappingEntry = keyMapping.entrySet()
          .iterator()
          .next();

      filterDefinition.path(keyMappingEntry.getValue())
          .valueExtractor(input -> input.get(keyMappingEntry.getKey()));
    } else {
      filterDefinition.path(Path.fromProperties(inverseRelation.getOriginRelation()))
          .valueExtractor(input -> extractKey(sourceType, input));
    }

    return filterDefinition.build();
  }

  private Set<SelectedProperty> selectIdentity(ObjectTypeRef typeRef) {
    return modelMapping.getSourceType(typeRef)
        .getIdentityProperties()
        .stream()
        .map(SelectedProperty::forProperty)
        .collect(toSet());
  }
}