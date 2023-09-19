package nl.geostandaarden.imx.orchestrate.engine.fetch;

import static java.util.Collections.unmodifiableSet;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toSet;
import static nl.geostandaarden.imx.orchestrate.engine.fetch.FetchUtils.isReservedField;
import static nl.geostandaarden.imx.orchestrate.model.ModelUtils.extractKey;

import graphql.schema.DataFetchingFieldSelectionSet;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import nl.geostandaarden.imx.orchestrate.engine.OrchestrateException;
import nl.geostandaarden.imx.orchestrate.engine.exchange.DataRequest;
import nl.geostandaarden.imx.orchestrate.engine.exchange.SelectedProperty;
import nl.geostandaarden.imx.orchestrate.engine.source.Source;
import nl.geostandaarden.imx.orchestrate.model.AbstractRelation;
import nl.geostandaarden.imx.orchestrate.model.Attribute;
import nl.geostandaarden.imx.orchestrate.model.InverseRelation;
import nl.geostandaarden.imx.orchestrate.model.ModelMapping;
import nl.geostandaarden.imx.orchestrate.model.ObjectType;
import nl.geostandaarden.imx.orchestrate.model.ObjectTypeRef;
import nl.geostandaarden.imx.orchestrate.model.Path;
import nl.geostandaarden.imx.orchestrate.model.PathMapping;
import nl.geostandaarden.imx.orchestrate.model.Property;
import nl.geostandaarden.imx.orchestrate.model.Relation;
import nl.geostandaarden.imx.orchestrate.model.filters.FilterDefinition;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public final class FetchPlanner {

  private final ModelMapping modelMapping;

  private final Map<String, Source> sources;

  private final UnaryOperator<String> lineageRenamer;

//  public Publisher<Map<String, Object>> fetch(DataFetchingEnvironment environment, GraphQLObjectType outputType) {
//    var targetType = modelMapping.getTargetModel()
//        .getObjectType(outputType.getName());
//    var targetMapping = modelMapping.getObjectTypeMapping(targetType.getName());
//    var sourcePaths = resolveSourcePaths(targetType, environment.getSelectionSet(), Path.fromProperties());
//
//    // TODO: Refactor
//    var isCollection = isList(unwrapNonNull(environment.getFieldType()));
//
//    var resultMapper = ObjectResultMapper.builder()
//        .modelMapping(modelMapping)
//        .build();
//
//    var input = FetchInput.newInput(keyExtractor(targetType, targetMapping)
//        .apply(environment.getArguments()));
//
//    var filter = createFilterDefinition(targetType, castToMap(environment.getArguments()
//        .get(QUERY_FILTER_ARGUMENTS)));
//
//    var fetchResult = fetchSourceObject(targetMapping.getSourceRoot(), sourcePaths, isCollection, filter)
//        .execute(input)
//        .map(result -> resultMapper.map(result, targetType, environment.getSelectionSet()));
//
//    return isCollection ? fetchResult : fetchResult.singleOrEmpty();
//  }

  public Publisher<Map<String, Object>> fetch(DataRequest request, Map<String, Object> arguments) {
    return Mono.empty();
  }

  private Set<Path> resolveSourcePaths(ObjectType objectType, DataFetchingFieldSelectionSet selectionSet,
                                       Path basePath) {
    var objectTypeMapping = modelMapping.getObjectTypeMapping(objectType);

    return selectionSet.getImmediateFields()
        .stream()
        .filter(not(field -> isReservedField(field, lineageRenamer)))
        .flatMap(field -> {
          var property = objectType.getProperty(field.getName());
          var propertyMapping = objectTypeMapping.getPropertyMapping(property);

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

          if (property instanceof AbstractRelation relation) {
            var targetType = modelMapping.getTargetType(relation.getTarget());

            return sourcePaths.flatMap(sourcePath ->
                resolveSourcePaths(targetType, field.getSelectionSet(), sourcePath).stream());
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
        .map(property -> SelectedProperty.builder()
            .property(property)
            .build())
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

              selectedProperties.add(SelectedProperty.builder()
                  .property(filterProperty)
                  .build());

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

                    selectedProperties.add(SelectedProperty.builder()
                        .property(keyProperty)
                        .build());
                  });
//            } else {
//              selectedProperties.add(new SelectedProperty(property, selectIdentity(targetTypeRef)));
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

    if (isCollection) {
      return CollectionFetchOperation.builder()
          .source(source)
          .objectType(sourceType)
          .selectedProperties(unmodifiableSet(selectedProperties))
          .nextOperations(unmodifiableSet(nextOperations))
          .filter(filter)
          .build();
    }

    return ObjectFetchOperation.builder()
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
        .map(property -> {
//          if (property instanceof Relation relation) {
//            return new SelectedProperty(property, selectIdentity(relation.getTarget(typeRef)));
//          }

          return SelectedProperty.builder()
              .property(property)
              .build();
        })
        .collect(toSet());
  }
}
