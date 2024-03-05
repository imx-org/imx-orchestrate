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
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import nl.geostandaarden.imx.orchestrate.engine.OrchestrateException;
import nl.geostandaarden.imx.orchestrate.engine.exchange.BatchRequest;
import nl.geostandaarden.imx.orchestrate.engine.exchange.CollectionRequest;
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
import nl.geostandaarden.imx.orchestrate.model.PropertyMapping;
import nl.geostandaarden.imx.orchestrate.model.Relation;
import nl.geostandaarden.imx.orchestrate.model.filters.FilterExpression;
import nl.geostandaarden.imx.orchestrate.model.filters.FilterOperator;
import reactor.core.publisher.Flux;

@RequiredArgsConstructor
public final class FetchPlanner {

  private final ModelMapping modelMapping;

  private final Map<String, Source> sources;

  public Flux<ObjectResult> fetch(ObjectRequest request) {
    var typeMappings = modelMapping.getObjectTypeMappings(request.getObjectType());
    var input = FetchInput.newInput(request.getObjectKey());

    return Flux.fromIterable(typeMappings)
        .flatMapSequential(typeMapping -> {
          var sourcePaths = resolveSourcePaths(request, typeMapping, Path.fromProperties());

          var resultMapper = ObjectResultMapper.builder()
              .modelMapping(modelMapping)
              .build();

          return fetchSourceObject(typeMapping.getSourceRoot(), sourcePaths, false, null)
              .execute(input)
              .map(result -> resultMapper.map(result, request));
        });
  }

  public Flux<ObjectResult> fetch(CollectionRequest request) {
    var typeMappings = modelMapping.getObjectTypeMappings(request.getObjectType());

    return Flux.fromIterable(typeMappings)
        .flatMapSequential(typeMapping -> {
          var sourceRoot = typeMapping.getSourceRoot();
          var sourcePaths = resolveSourcePaths(request, typeMapping, Path.fromProperties());

          var resultMapper = ObjectResultMapper.builder()
              .modelMapping(modelMapping)
              .build();

          var filterMapper = Optional.ofNullable(request.getFilter())
              .map(FilterMapper::just)
              .orElse(null);

          return fetchSourceObject(sourceRoot, sourcePaths, true, filterMapper)
              .execute(FetchInput.newInput(Map.of()))
              .map(result -> resultMapper.map(result, request));
        });
  }

  public Flux<ObjectResult> fetch(BatchRequest request) {
    var typeMappings = modelMapping.getObjectTypeMappings(request.getObjectType());

    var inputs = request.getObjectKeys()
        .stream()
        .map(FetchInput::newInput)
        .toList();

    return Flux.fromIterable(typeMappings)
        .flatMapSequential(typeMapping -> {
          var sourcePaths = resolveSourcePaths(request, typeMapping, Path.fromProperties());

          var resultMapper = ObjectResultMapper.builder()
              .modelMapping(modelMapping)
              .build();

          return fetchSourceObject(typeMapping.getSourceRoot(), sourcePaths, false, null)
              .executeBatch(inputs)
              .map(result -> resultMapper.map(result, request));
        });
  }

  private Set<Path> resolveSourcePaths(DataRequest request, ObjectTypeMapping typeMapping, Path basePath) {
    var sourceTypeRef = typeMapping.getSourceRoot();

    return request.getSelectedProperties()
        .stream()
        .flatMap(selectedProperty -> resolveSourcePaths(sourceTypeRef, typeMapping, selectedProperty, basePath))
        .collect(toSet());
  }

  private Stream<Path> resolveSourcePaths(ObjectTypeRef sourceTypeRef, ObjectTypeMapping typeMapping, SelectedProperty selectedProperty, Path basePath) {
    var propertyMappingOptional = typeMapping.getPropertyMapping(selectedProperty.getProperty());

    if (propertyMappingOptional.isPresent()) {
      return propertyMappingOptional.stream()
          .flatMap(propertyMapping -> resolveSourcePaths(sourceTypeRef, propertyMapping, selectedProperty, basePath));
    }

    if (selectedProperty.getProperty() instanceof AbstractRelation relation) {
      var relTargetType = modelMapping.getTargetType(relation.getTarget());

      return modelMapping
          .getObjectTypeMappings(relTargetType)
          .stream()
          .filter(targetTypeMapping -> targetTypeMapping.getSourceRoot()
              .equals(typeMapping.getSourceRoot()))
          .flatMap(targetTypeMapping -> resolveSourcePaths(selectedProperty.getNestedRequest(), targetTypeMapping, basePath).stream());
    }

    return Stream.empty();
  }

  private Stream<Path> resolveSourcePaths(ObjectTypeRef sourceTypeRef, PropertyMapping propertyMapping, SelectedProperty selectedProperty, Path basePath) {
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

    if (selectedProperty.getProperty() instanceof AbstractRelation relation) {
      var nestedRequest = Optional.ofNullable(selectedProperty.getNestedRequest())
          .orElseThrow(() -> new OrchestrateException("Nested request not present for relation: " + relation.getName()));

      return sourcePaths.flatMap(sourcePath -> {
        var sourceRelTarget = resolveSourceRelation(sourceTypeRef, sourcePath).getTarget();

        var nestedTypeMapping = modelMapping.getObjectTypeMapping(nestedRequest.getObjectType(), sourceRelTarget)
            .orElseThrow(() -> new OrchestrateException("Type mapping not found for source root: " + sourceRelTarget));

        return resolveSourcePaths(nestedRequest, nestedTypeMapping, Path.fromProperties())
            .stream()
            .map(sourcePath::append);
      });
    }

    return sourcePaths;
  }

  private AbstractRelation resolveSourceRelation(ObjectTypeRef sourceTypeRef, Path sourcePath) {
    var sourceType = modelMapping.getSourceType(sourceTypeRef);
    var property = sourceType.getProperty(sourcePath.getFirstSegment());

    if (property instanceof AbstractRelation relation) {
      if (sourcePath.isLeaf()) {
        return relation;
      }

      return resolveSourceRelation(relation.getTarget(sourceTypeRef), sourcePath.withoutFirstSegment());
    }

    throw new OrchestrateException("Path segment is not a relation: " + sourcePath.getFirstSegment());
  }

  private FetchOperation fetchSourceObject(ObjectTypeRef sourceTypeRef, Set<Path> sourcePaths, boolean isCollection, FilterMapper filterMapper) {
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
            var relFilterMapper = createFilterMapper(sourceType, inverseRelation);
            var originTypeRef = inverseRelation.getTarget(sourceTypeRef);

            nextOperations.add(NextOperation.builder()
                .property(inverseRelation)
                .delegateOperation(fetchSourceObject(originTypeRef, nestedSourcePaths, true, relFilterMapper))
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
                FilterMapper relFilterMapper = input -> FilterExpression.builder()
                    .path(Path.fromProperties(targetProperty))
                    .operator(FilterOperator.builder()
                        .type(filterMapping.getOperator())
                        .build())
                    .value(targetAttribute.getType()
                        .mapSourceValue(input.getData().get(sourcePath.getFirstSegment())))
                    .build();

                nextOperations.add(NextOperation.builder()
                    .property(relation)
                    .delegateOperation(fetchSourceObject(targetTypeRef, nestedSourcePaths, true, relFilterMapper))
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
                selectedProperties.add(SelectedProperty.forProperty(property, ObjectRequest.builder(targetModel)
                    .objectType(targetTypeRef)
                    .objectKey(Map.of())
                    .selectedProperties(selectIdentity(targetTypeRef))
                    .build()));
              } else {
                // TODO: Filter
                selectedProperties.add(SelectedProperty.forProperty(property, CollectionRequest.builder(targetModel)
                    .objectType(targetTypeRef)
                    .selectedProperties(selectIdentity(targetTypeRef))
                    .build()));
              }
            }

            // TODO: Disabled for now, since result mapper handles this incorrectly
            // var identityPaths = targetType.getIdentityProperties()
            //     .stream()
            //     .map(Path::fromProperties)
            //     .collect(Collectors.toSet());

            // If only identity is selected, no next operation is needed
            // if (identityPaths.equals(nestedSourcePaths)) {
            //   return;
            // }

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
          .filterMapper(filterMapper)
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

  private FilterMapper createFilterMapper(ObjectType sourceType, InverseRelation inverseRelation) {
    var keyMapping = inverseRelation.getOriginRelation()
        .getKeyMapping();

    if (keyMapping != null) {
      // TODO: Composite keys
      var keyMappingEntry = keyMapping.entrySet()
          .iterator()
          .next();

      return input -> FilterExpression.builder()
          .path(keyMappingEntry.getValue())
          .value(input.getData().get(keyMappingEntry.getKey()))
          .build();
    }

    return input -> FilterExpression.builder()
        .path(Path.fromProperties(inverseRelation.getOriginRelation()))
        .value(extractKey(sourceType, input.getData()))
        .build();
  }

  private Set<SelectedProperty> selectIdentity(ObjectTypeRef typeRef) {
    return modelMapping.getSourceType(typeRef)
        .getIdentityProperties()
        .stream()
        .map(SelectedProperty::forProperty)
        .collect(toSet());
  }
}
