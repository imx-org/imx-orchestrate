package org.dotwebstack.orchestrate.engine.fetch;

import static graphql.schema.GraphQLTypeUtil.isList;
import static graphql.schema.GraphQLTypeUtil.unwrapNonNull;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableSet;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toSet;
import static org.dotwebstack.orchestrate.engine.fetch.FetchUtils.cast;
import static org.dotwebstack.orchestrate.engine.fetch.FetchUtils.extractKey;
import static org.dotwebstack.orchestrate.engine.fetch.FetchUtils.isReservedField;
import static org.dotwebstack.orchestrate.engine.fetch.FetchUtils.keyExtractor;
import static org.dotwebstack.orchestrate.engine.fetch.FetchUtils.propertyExtractor;
import static org.dotwebstack.orchestrate.engine.fetch.FetchUtils.selectIdentity;

import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.DataFetchingFieldSelectionSet;
import graphql.schema.GraphQLObjectType;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.dotwebstack.orchestrate.engine.OrchestrateException;
import org.dotwebstack.orchestrate.model.AbstractRelation;
import org.dotwebstack.orchestrate.model.InverseRelation;
import org.dotwebstack.orchestrate.model.ModelMapping;
import org.dotwebstack.orchestrate.model.ObjectType;
import org.dotwebstack.orchestrate.model.ObjectTypeRef;
import org.dotwebstack.orchestrate.model.Property;
import org.dotwebstack.orchestrate.model.PropertyPath;
import org.dotwebstack.orchestrate.model.Relation;
import org.dotwebstack.orchestrate.source.FilterDefinition;
import org.dotwebstack.orchestrate.source.SelectedProperty;
import org.dotwebstack.orchestrate.source.Source;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

@RequiredArgsConstructor
public final class FetchPlanner {

  private final ModelMapping modelMapping;

  private final Map<String, Source> sources;

  private final ObjectMapper lineageMapper;

  private final UnaryOperator<String> lineageRenamer;

  public Publisher<Map<String, Object>> fetch(DataFetchingEnvironment environment, GraphQLObjectType outputType) {
    var targetType = modelMapping.getTargetModel()
        .getObjectType(outputType.getName());
    var targetMapping = modelMapping.getObjectTypeMapping(targetType.getName());
    var sourcePaths = resolveSourcePaths(targetType, environment.getSelectionSet(), PropertyPath.fromProperties());

    var propertyMappings = environment.getSelectionSet()
        .getImmediateFields()
        .stream()
        .filter(not(field -> isReservedField(field, lineageRenamer)))
        .map(selectedField -> targetType.getProperty(selectedField.getName()))
        .collect(Collectors.toMap(Function.identity(),
            property -> targetMapping.getPropertyMapping(property.getName())));

    // TODO: Refactor
    var isCollection = isList(unwrapNonNull(environment.getFieldType()));

    var resultMapper = ObjectResultMapper.builder()
        .targetType(targetType)
        .propertyMappings(propertyMappings)
        .build();

    Map<String, Object> parentData = environment.getSource();

    if (parentData != null && isCollection) {
      List<Map<String, Object>> inputData = cast(parentData.get(environment.getField()
          .getName()));

      if (inputData == null) {
        return Flux.empty();
      }

      return Flux.fromIterable(inputData)
          .flatMap(objectKey -> fetchSourceObject(targetMapping.getSourceRoot(), sourcePaths, false, null)
              .execute(FetchInput.newInput(objectKey))
              .map(resultMapper)
              .map(result -> result.toMap(lineageMapper, lineageRenamer)));
    }

    var input = FetchInput.newInput(keyExtractor(targetType, targetMapping)
        .apply(environment.getArguments()));

    var fetchResult = fetchSourceObject(targetMapping.getSourceRoot(), sourcePaths, isCollection, null)
        .execute(input)
        .map(resultMapper)
        .map(result -> result.toMap(lineageMapper, lineageRenamer));

    return isCollection ? fetchResult : fetchResult.singleOrEmpty();
  }

  public Set<PropertyPath> resolveSourcePaths(ObjectType objectType, DataFetchingFieldSelectionSet selectionSet,
      PropertyPath basePath) {
    var objectTypeMapping = modelMapping.getObjectTypeMapping(objectType);

    return selectionSet.getImmediateFields()
        .stream()
        .filter(not(field -> isReservedField(field, lineageRenamer)))
        .flatMap(field -> {
          var property = objectType.getProperty(field.getName());
          var propertyMapping = objectTypeMapping.getPropertyMapping(property);

          var sourcePaths = propertyMapping.getPathMappings()
              .stream()
              .flatMap(pathMapping -> pathMapping.getPaths()
                  .stream()
                  .map(basePath::append));

          if (property instanceof AbstractRelation relation) {
            var targetType = modelMapping.getTargetType(relation.getTarget());

            return sourcePaths.flatMap(sourcePath ->
                resolveSourcePaths(targetType, field.getSelectionSet(), sourcePath).stream());
          }

          return sourcePaths;
        })
        .collect(toSet());
  }

  private FetchOperation fetchSourceObject(ObjectTypeRef sourceTypeRef, Set<PropertyPath> sourcePaths,
      boolean isCollection, FilterDefinition filter) {
    var source = sources.get(sourceTypeRef.getModelAlias());
    var sourceType = modelMapping.getSourceType(sourceTypeRef);
    var selectedProperties = new ArrayList<>(selectIdentity(sourceType));

    sourcePaths.stream()
        .filter(PropertyPath::isLeaf)
        .map(sourcePath -> sourceType.getProperty(sourcePath.getFirstSegment()))
        .filter(not(Property::isIdentifier))
        .map(SelectedProperty::new)
        .forEach(selectedProperties::add);

    var nextOperations = new HashSet<NextOperation>();

    sourcePaths.stream()
        .filter(not(PropertyPath::isLeaf))
        .collect(groupingBy(PropertyPath::getFirstSegment, mapping(PropertyPath::withoutFirstSegment, toSet())))
        .forEach((propertyName, nestedSourcePaths) -> {
          var property = sourceType.getProperty(propertyName);

          if (property instanceof InverseRelation inverseRelation) {
            var filterDefinition = FilterDefinition.builder()
                .propertyPath(PropertyPath.fromProperties(inverseRelation.getOriginRelation()))
                .valueExtractor(input -> extractKey(sourceType, input))
                .build();

            var originTypeRef = inverseRelation.getTarget(sourceTypeRef);

            nextOperations.add(NextOperation.builder()
                .property(inverseRelation)
                .delegateOperation(fetchSourceObject(originTypeRef, nestedSourcePaths, true, filterDefinition))
                .inputMapper(keyExtractor(sourceType))
                .build());

            return;
          }

          if (property instanceof Relation relation) {
            var targetTypeRef = relation.getTarget(sourceTypeRef);
            var targetType = modelMapping.getSourceType(targetTypeRef);

            selectedProperties.add(new SelectedProperty(property, selectIdentity(targetType)));

            var identityPropertyPaths = targetType.getIdentityProperties()
                .stream()
                .map(PropertyPath::fromProperties)
                .collect(Collectors.toSet());

            // If only identity is selected, no next operation is needed
            if (identityPropertyPaths.equals(nestedSourcePaths)) {
              return;
            }

            nextOperations.add(NextOperation.builder()
                .property(relation)
                .delegateOperation(fetchSourceObject(targetTypeRef, nestedSourcePaths, false, null))
                .inputMapper(propertyExtractor(propertyName))
                .build());

            return;
          }

          throw new OrchestrateException("Could not map property: " + propertyName);
        });

    if (isCollection) {
      return CollectionFetchOperation.builder()
          .source(source)
          .objectType(sourceType)
          .selectedProperties(unmodifiableList(selectedProperties))
          .nextOperations(unmodifiableSet(nextOperations))
          .filter(filter)
          .build();
    }

    return ObjectFetchOperation.builder()
        .source(source)
        .objectType(sourceType)
        .selectedProperties(unmodifiableList(selectedProperties))
        .nextOperations(unmodifiableSet(nextOperations))
        .build();
  }
}
