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
import static org.dotwebstack.orchestrate.engine.fetch.FetchUtils.propertyExtractor;
import static org.dotwebstack.orchestrate.engine.fetch.FetchUtils.selectIdentity;

import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLObjectType;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.UnaryOperator;
import lombok.RequiredArgsConstructor;
import org.dotwebstack.orchestrate.engine.OrchestrateException;
import org.dotwebstack.orchestrate.model.InverseRelation;
import org.dotwebstack.orchestrate.model.ModelMapping;
import org.dotwebstack.orchestrate.model.ObjectTypeRef;
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

  private final ObjectMapper objectMapper;

  private final UnaryOperator<String> lineageRenamer;

  public Publisher<Map<String, Object>> fetch(DataFetchingEnvironment environment, GraphQLObjectType outputType) {
    var targetType = modelMapping.getTargetModel()
        .getObjectType(outputType.getName());

    var targetMapping = modelMapping.getObjectTypeMapping(targetType.getName());
    var propertyMappings = new LinkedHashMap<Property, PropertyMapping>();
    var sourcePaths = new HashSet<PropertyPath>();

    environment.getSelectionSet()
        .getImmediateFields()
        .stream()
        .filter(not(field -> FetchUtils.isReservedField(field, lineageRenamer)))
        .map(property -> targetType.getProperty(property.getName()))
        .forEach(property -> {
          var propertyMapping = targetMapping.getPropertyMapping(property.getName());
          propertyMappings.put(property, propertyMapping);
          propertyMapping.getPathMappings()
              .forEach(pathMapping -> sourcePaths.addAll(pathMapping.getPaths()));
        });

    // TODO: Refactor
    var isCollection = isList(unwrapNonNull(environment.getFieldType()));

    var resultMapper = ObjectResultMapper.builder()
        .targetType(targetType)
        .propertyMappings(propertyMappings)
        .build();

    var fetchOperation = fetchSourceObject(targetMapping.getSourceRoot(), unmodifiableSet(sourcePaths), isCollection,
        resultMapper);

    var context = FetchInput.builder()
        .data(keyExtractor(targetType, targetMapping)
            .apply(environment.getArguments()))
        .build();

    var fetchResult = fetchOperation.execute(context)
        .map(objectResult -> objectResult.toMap(objectMapper, lineageRenamer));

    return isCollection ? fetchResult : fetchResult.singleOrEmpty();
  }

  private FetchOperation fetchSourceObject(ObjectTypeRef sourceTypeRef, Set<PropertyPath> sourcePaths,
      boolean isCollection, UnaryOperator<ObjectResult> resultMapper) {
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
            var originTypeRef = inverseRelation.getTarget(sourceTypeRef);
            var originType = modelMapping.getSourceType(originTypeRef);
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
                .property(inverseRelation)
                .delegateOperation(CollectionFetchOperation.builder()
                    .source(source)
                    .objectType(originType)
                    .filter(filter)
                    .selectedProperties(nestedProperties)
                    .build())
                .inputMapper(keyExtractor(sourceType))
                .build());

            return;
          }

          if (property instanceof Relation relation) {
            var targetTypeRef = relation.getTarget(sourceTypeRef);
            var targetType = modelMapping.getSourceType(targetTypeRef);

            selectedProperties.add(new SelectedProperty(property, selectIdentity(targetType)));

            nextOperations.add(NextOperation.builder()
                .property(relation)
                .delegateOperation(fetchSourceObject(targetTypeRef, nestedSourcePaths, false,
                    UnaryOperator.identity()))
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
          .resultMapper(resultMapper)
          .nextOperations(unmodifiableSet(nextOperations))
          .build();
    }

    return ObjectFetchOperation.builder()
        .source(source)
        .objectType(sourceType)
        .selectedProperties(unmodifiableList(selectedProperties))
        .resultMapper(resultMapper)
        .nextOperations(unmodifiableSet(nextOperations))
        .build();
  }
}
