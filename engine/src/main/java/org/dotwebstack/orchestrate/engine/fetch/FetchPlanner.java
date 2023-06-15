package org.dotwebstack.orchestrate.engine.fetch;

import static graphql.schema.GraphQLTypeUtil.isList;
import static graphql.schema.GraphQLTypeUtil.unwrapNonNull;
import static java.util.Collections.unmodifiableSet;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toSet;
import static org.dotwebstack.orchestrate.engine.fetch.FetchUtils.castToMap;
import static org.dotwebstack.orchestrate.engine.fetch.FetchUtils.isReservedField;
import static org.dotwebstack.orchestrate.engine.schema.SchemaConstants.QUERY_FILTER_ARGUMENTS;
import static org.dotwebstack.orchestrate.model.ModelUtils.extractKey;
import static org.dotwebstack.orchestrate.model.ModelUtils.keyExtractor;

import graphql.schema.DataFetchingEnvironment;
import graphql.schema.DataFetchingFieldSelectionSet;
import graphql.schema.GraphQLObjectType;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.dotwebstack.orchestrate.engine.OrchestrateException;
import org.dotwebstack.orchestrate.model.AbstractRelation;
import org.dotwebstack.orchestrate.model.Attribute;
import org.dotwebstack.orchestrate.model.InverseRelation;
import org.dotwebstack.orchestrate.model.ModelMapping;
import org.dotwebstack.orchestrate.model.ObjectType;
import org.dotwebstack.orchestrate.model.ObjectTypeRef;
import org.dotwebstack.orchestrate.model.Path;
import org.dotwebstack.orchestrate.model.PathMapping;
import org.dotwebstack.orchestrate.model.Property;
import org.dotwebstack.orchestrate.model.Relation;
import org.dotwebstack.orchestrate.model.filters.FilterDefinition;
import org.dotwebstack.orchestrate.source.SelectedProperty;
import org.dotwebstack.orchestrate.source.Source;
import org.reactivestreams.Publisher;

@RequiredArgsConstructor
public final class FetchPlanner {

  private final ModelMapping modelMapping;

  private final Map<String, Source> sources;

  private final UnaryOperator<String> lineageRenamer;

  public Publisher<Map<String, Object>> fetch(DataFetchingEnvironment environment, GraphQLObjectType outputType) {
    var targetType = modelMapping.getTargetModel()
        .getObjectType(outputType.getName());
    var targetMapping = modelMapping.getObjectTypeMapping(targetType.getName());
    var sourcePaths = resolveSourcePaths(targetType, environment.getSelectionSet(), Path.fromProperties());

    // TODO: Refactor
    var isCollection = isList(unwrapNonNull(environment.getFieldType()));

    var resultMapper = ObjectResultMapper.builder()
        .modelMapping(modelMapping)
        .build();

    var input = FetchInput.newInput(keyExtractor(targetType, targetMapping)
        .apply(environment.getArguments()));

    var filter = createFilterDefinition(targetType, castToMap(environment.getArguments()
        .get(QUERY_FILTER_ARGUMENTS)));

    var fetchResult = fetchSourceObject(targetMapping.getSourceRoot(), sourcePaths, isCollection, filter)
        .execute(input)
        .map(result -> resultMapper.map(result, targetType, environment.getSelectionSet()));

    return isCollection ? fetchResult : fetchResult.singleOrEmpty();
  }

  public Set<Path> resolveSourcePaths(ObjectType objectType, DataFetchingFieldSelectionSet selectionSet,
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
        .map(SelectedProperty::new)
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
              selectedProperties.add(new SelectedProperty(filterProperty));

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
                    selectedProperties.add(new SelectedProperty(keyProperty));
                  });
            } else {
              selectedProperties.add(new SelectedProperty(property, selectIdentity(targetTypeRef)));
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

    var pathmapping = pathMappings.get(0);
    var path = pathmapping.getPath();

    if (!path.isLeaf()) {
      throw new OrchestrateException("Currently only direct source root properties can be filtered.");
    }

    return ((Attribute) targetType.getProperty(firstEntry.getKey()))
        .getType()
        .createFilterDefinition(pathmapping.getPath(), firstEntry.getValue());
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
          if (property instanceof Relation relation) {
            return new SelectedProperty(property, selectIdentity(relation.getTarget(typeRef)));
          }

          return new SelectedProperty(property);
        })
        .collect(toSet());
  }
}
