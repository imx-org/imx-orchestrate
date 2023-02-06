package org.dotwebstack.orchestrate.engine.fetch;

import static graphql.schema.GraphQLTypeUtil.isList;
import static graphql.schema.GraphQLTypeUtil.unwrapNonNull;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.dotwebstack.orchestrate.engine.fetch.FetchUtils.keyExtractor;
import static org.dotwebstack.orchestrate.engine.fetch.FetchUtils.selectIdentify;

import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLObjectType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.dotwebstack.orchestrate.model.Property;
import org.dotwebstack.orchestrate.model.PropertyPath;
import org.dotwebstack.orchestrate.model.ModelMapping;
import org.dotwebstack.orchestrate.model.PropertyMapping;
import org.dotwebstack.orchestrate.model.ObjectType;
import org.dotwebstack.orchestrate.model.Relation;
import org.dotwebstack.orchestrate.source.SelectedProperty;
import org.dotwebstack.orchestrate.source.Source;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public final class FetchPlanner {

  private final ModelMapping modelMapping;

  private final Map<String, Source> sources;

  public Publisher<Map<String, Object>> fetch(DataFetchingEnvironment environment, GraphQLObjectType outputType) {
    var targetType = modelMapping.getTargetModel()
        .getObjectType(outputType.getName());

    var targetMapping = modelMapping.getObjectTypeMappings()
        .get(targetType.getName());

    var propertyMappings = new HashMap<Property, PropertyMapping>();
    var sourcePaths = new ArrayList<PropertyPath>();

    environment.getSelectionSet()
        .getImmediateFields()
        .stream()
        .filter(not(FetchUtils::isIntrospectionField))
        .map(property -> targetType.getProperty(property.getName()))
        .forEach(property -> {
          var propertyMapping = targetMapping.getPropertyMapping(property.getName());
          propertyMappings.put(property, propertyMapping);
          sourcePaths.addAll(propertyMapping.getSourcePaths());
        });

    var sourceRoot = targetMapping.getSourceRoot();

    var sourceType = modelMapping.getSourceModels()
        .get(sourceRoot.getModelAlias())
        .getObjectType(sourceRoot.getObjectType());

    // TODO: Refactor
    var isCollection = isList(unwrapNonNull(environment.getFieldType()));
    var fetchPublisher = fetchSourceObject(sourceType, unmodifiableList(sourcePaths), sourceRoot.getModelAlias(),
        isCollection).execute(environment.getArguments());

    if (fetchPublisher instanceof Mono<?>) {
      return Mono.from(fetchPublisher)
          .map(result -> mapResult(unmodifiableMap(propertyMappings), result));
    }

    return Flux.from(fetchPublisher)
        .map(result -> mapResult(unmodifiableMap(propertyMappings), result));
  }

  private FetchOperation fetchSourceObject(ObjectType sourceType, List<PropertyPath> sourcePaths, String sourceAlias,
      boolean isCollection) {
    var selectedProperties = new ArrayList<SelectedProperty>();

    sourcePaths.stream()
        .filter(PropertyPath::isLeaf)
        .filter(not(PropertyPath::hasOrigin))
        .map(sourcePath -> new SelectedProperty(sourceType.getProperty(sourcePath.getFirstSegment())))
        .forEach(selectedProperties::add);

    var nextOperations = new HashMap<String, FetchOperation>();

    sourcePaths.stream()
        .filter(not(PropertyPath::isLeaf))
        .filter(not(PropertyPath::hasOrigin))
        .collect(groupingBy(PropertyPath::getFirstSegment, mapping(PropertyPath::withoutFirstSegment, toList())))
        .forEach((propertyName, nestedSourcePaths) -> {
          var property = sourceType.getProperty(propertyName);

          // TODO: Differing model aliases & type safety
          var nestedObjectType = modelMapping.getSourceModel(sourceAlias)
              .getObjectType(((Relation) property).getTarget());

          selectedProperties.add(new SelectedProperty(property, selectIdentify(nestedObjectType)));
          nextOperations.put(propertyName, fetchSourceObject(nestedObjectType, nestedSourcePaths, sourceAlias, false));
        });

    if (isCollection) {
      return CollectionFetchOperation.builder()
          .source(sources.get(sourceAlias))
          .objectType(sourceType)
          .selectedProperties(unmodifiableList(selectedProperties))
          .nextOperations(unmodifiableMap(nextOperations))
          .build();
    }

    return ObjectFetchOperation.builder()
        .source(sources.get(sourceAlias))
        .objectType(sourceType)
        .selectedProperties(unmodifiableList(selectedProperties))
        .keyExtractor(keyExtractor(sourceType))
        .nextOperations(unmodifiableMap(nextOperations))
        .build();
  }

  private Map<String, Object> mapResult(Map<Property, PropertyMapping> propertyMappings, Map<String, Object> result) {
    return propertyMappings.entrySet()
        .stream()
        .collect(toMap(entry -> entry.getKey().getName(), entry -> mapPropertyResult(entry.getValue(), result)));
  }

  private Object mapPropertyResult(PropertyMapping propertyMapping, Map<String, Object> result) {
    return propertyMapping.getSourcePaths()
        .stream()
        .map(sourcePath -> mapPropertyResult(sourcePath, result))
        .filter(Objects::nonNull)
        .findFirst();
  }

  private Object mapPropertyResult(PropertyPath sourcePath, Map<String, Object> result) {
    var firstSegment = sourcePath.getFirstSegment();

    if (sourcePath.isLeaf()) {
      return result.get(firstSegment);
    }

    @SuppressWarnings("unchecked")
    var propertyValue = (Map<String, Object>) result.get(firstSegment);

    if (propertyValue == null) {
      return null;
    }

    return mapPropertyResult(sourcePath.withoutFirstSegment(), propertyValue);
  }
}
