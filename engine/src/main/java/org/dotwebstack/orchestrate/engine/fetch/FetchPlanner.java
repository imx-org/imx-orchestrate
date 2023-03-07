package org.dotwebstack.orchestrate.engine.fetch;

import static graphql.schema.GraphQLTypeUtil.isList;
import static graphql.schema.GraphQLTypeUtil.unwrapNonNull;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableSet;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toSet;
import static org.dotwebstack.orchestrate.engine.fetch.FetchUtils.keyExtractor;
import static org.dotwebstack.orchestrate.engine.fetch.FetchUtils.selectIdentity;

import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLObjectType;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.UnaryOperator;
import lombok.RequiredArgsConstructor;
import org.dotwebstack.orchestrate.model.InverseRelation;
import org.dotwebstack.orchestrate.model.ModelMapping;
import org.dotwebstack.orchestrate.model.ObjectType;
import org.dotwebstack.orchestrate.model.Property;
import org.dotwebstack.orchestrate.model.PropertyMapping;
import org.dotwebstack.orchestrate.model.PropertyPath;
import org.dotwebstack.orchestrate.model.Relation;
import org.dotwebstack.orchestrate.source.FilterDefinition;
import org.dotwebstack.orchestrate.source.SelectedProperty;
import org.dotwebstack.orchestrate.source.Source;
import org.reactivestreams.Publisher;

@RequiredArgsConstructor
public final class FetchPlanner {

  private final ModelMapping modelMapping;

  private final Map<String, Source> sources;

  public Publisher<Map<String, Object>> fetch(DataFetchingEnvironment environment, GraphQLObjectType outputType) {
    var targetType = modelMapping.getTargetModel()
        .getObjectType(outputType.getName());

    var targetMapping = modelMapping.getObjectTypeMapping(targetType.getName());
    var propertyMappings = new LinkedHashMap<Property, PropertyMapping>();
    var sourcePaths = new HashSet<PropertyPath>();

    environment.getSelectionSet()
        .getImmediateFields()
        .stream()
        .filter(not(FetchUtils::isReservedField))
        .map(property -> targetType.getProperty(property.getName()))
        .forEach(property -> {
          var propertyMapping = targetMapping.getPropertyMapping(property.getName());
          propertyMappings.put(property, propertyMapping);
          propertyMapping.getPathMappings()
              .forEach(pathMapping -> sourcePaths.addAll(pathMapping.getPaths()));
        });

    var sourceType = modelMapping.getSourceType(targetMapping.getSourceRoot());

    // TODO: Refactor
    var isCollection = isList(unwrapNonNull(environment.getFieldType()));

    var resultMapper = ObjectResultMapper.builder()
        .targetType(targetType)
        .propertyMappings(propertyMappings)
        .build();

    var fetchOperation = fetchSourceObject(sourceType, unmodifiableSet(sourcePaths), targetMapping.getSourceRoot()
        .getModelAlias(), isCollection, resultMapper);

    var context = FetchContext.builder()
        .input(keyExtractor(targetType, targetMapping)
            .apply(environment.getArguments()))
        .build();

    var fetchResult = fetchOperation.execute(context)
        .map(ObjectResult::toMap);

    return isCollection ? fetchResult : fetchResult.singleOrEmpty();
  }

  private FetchOperation fetchSourceObject(ObjectType sourceType, Set<PropertyPath> sourcePaths, String sourceAlias,
      boolean isCollection,
      UnaryOperator<ObjectResult> resultMapper) {
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
            var originType = modelMapping.getSourceModel(sourceAlias)
                .getObjectType(inverseRelation.getTarget()
                    .getName());

            var originFieldName = inverseRelation.getOriginRelation()
                .getName();

            // TODO: How to handle composite keys?
            var filter = FilterDefinition.builder()
                .propertyPath(nestedSourcePaths.iterator()
                    .next()
                    .prependSegment(originFieldName))
                .valueExtractor(input -> input.get(sourceType.getIdentityProperties()
                    .get(0)
                    .getName()))
                .build();

            var nestedProperties = nestedSourcePaths.stream()
                .map(sourcePath -> new SelectedProperty(originType.getProperty(sourcePath.getFirstSegment())))
                .toList();

            nextOperations.add(NextOperation.builder()
                .propertyName(propertyName)
                .delegateOperation(CollectionFetchOperation.builder()
                    .source(sources.get(sourceAlias))
                    .objectType(originType)
                    .filter(filter)
                    .selectedProperties(nestedProperties)
                    .build())
                .inputMapper(FetchUtils.inputMapper(sourceType))
                .singleResult(true)
                .build());

            return;
          }

          // TODO: Differing model aliases & type safety
          var relatedObjectType = modelMapping.getSourceModel(sourceAlias)
              .getObjectType(((Relation) property).getTarget()
                  .getName());

          selectedProperties.add(new SelectedProperty(property, selectIdentity(relatedObjectType)));

          nextOperations.add(NextOperation.builder()
              .propertyName(propertyName)
              .delegateOperation(fetchSourceObject(relatedObjectType, nestedSourcePaths, sourceAlias, false,
                  UnaryOperator.identity()))
              .inputMapper(FetchUtils.inputMapper(propertyName))
              .singleResult(true)
              .build());
        });

    if (isCollection) {
      return CollectionFetchOperation.builder()
          .source(sources.get(sourceAlias))
          .objectType(sourceType)
          .selectedProperties(unmodifiableList(selectedProperties))
          .resultMapper(resultMapper)
          .nextOperations(unmodifiableSet(nextOperations))
          .build();
    }

    return ObjectFetchOperation.builder()
        .source(sources.get(sourceAlias))
        .objectType(sourceType)
        .selectedProperties(unmodifiableList(selectedProperties))
        .resultMapper(resultMapper)
        .nextOperations(unmodifiableSet(nextOperations))
        .build();
  }
}
