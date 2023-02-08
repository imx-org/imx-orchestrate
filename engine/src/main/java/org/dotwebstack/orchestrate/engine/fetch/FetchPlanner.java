package org.dotwebstack.orchestrate.engine.fetch;

import static graphql.schema.GraphQLTypeUtil.isList;
import static graphql.schema.GraphQLTypeUtil.unwrapNonNull;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toSet;
import static org.dotwebstack.orchestrate.engine.fetch.FetchUtils.inputMapper;
import static org.dotwebstack.orchestrate.engine.fetch.FetchUtils.keyExtractor;
import static org.dotwebstack.orchestrate.engine.fetch.FetchUtils.noopCombiner;
import static org.dotwebstack.orchestrate.engine.fetch.FetchUtils.pathValue;
import static org.dotwebstack.orchestrate.engine.fetch.FetchUtils.selectIdentity;

import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLObjectType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
import org.dotwebstack.orchestrate.model.transforms.Transform;
import org.dotwebstack.orchestrate.source.FilterDefinition;
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
    var sourcePaths = new HashSet<PropertyPath>();

    environment.getSelectionSet()
        .getImmediateFields()
        .stream()
        .filter(not(FetchUtils::isIntrospectionField))
        .map(property -> targetType.getProperty(property.getName()))
        .forEach(property -> {
          var propertyMapping = targetMapping.getPropertyMapping(property.getName());
          propertyMappings.put(property, propertyMapping);
          propertyMapping.getPathMappings()
              .forEach(pathMapping -> sourcePaths.addAll(pathMapping.getPaths()));
        });

    var sourceRoot = targetMapping.getSourceRoot();

    var sourceType = modelMapping.getSourceModels()
        .get(sourceRoot.getModelAlias())
        .getObjectType(sourceRoot.getObjectType());

    // TODO: Refactor
    var isCollection = isList(unwrapNonNull(environment.getFieldType()));
    var fetchPublisher = fetchSourceObject(sourceType, unmodifiableSet(sourcePaths), sourceRoot.getModelAlias(),
        isCollection, UnaryOperator.identity()).execute(environment.getArguments());

    if (fetchPublisher instanceof Mono<?>) {
      return Mono.from(fetchPublisher)
          .map(result -> mapResult(unmodifiableMap(propertyMappings), result));
    }

    return Flux.from(fetchPublisher)
        .map(result -> mapResult(unmodifiableMap(propertyMappings), result));
  }

  private FetchOperation fetchSourceObject(ObjectType sourceType, Set<PropertyPath> sourcePaths, String sourceAlias,
      boolean isCollection, UnaryOperator<Map<String, Object>> inputMapper) {
    var selectedProperties = new ArrayList<SelectedProperty>();

    sourcePaths.stream()
        .filter(PropertyPath::isLeaf)
        .map(sourcePath -> new SelectedProperty(sourceType.getProperty(sourcePath.getFirstSegment())))
        .forEach(selectedProperties::add);

    var nextOperations = new HashMap<String, FetchOperation>();

    sourcePaths.stream()
        .filter(not(PropertyPath::isLeaf))
        .collect(groupingBy(PropertyPath::getFirstSegment, mapping(PropertyPath::withoutFirstSegment, toSet())))
        .forEach((propertyName, nestedSourcePaths) -> {
          var property = sourceType.getProperty(propertyName);

          if (property instanceof InverseRelation relationProperty) {
            var originType = modelMapping.getSourceModel(sourceAlias)
                .getObjectType(relationProperty.getTarget());

            var filter = FilterDefinition.builder()
                .propertyPath(nestedSourcePaths.iterator()
                    .next()
                    .prependSegment(propertyName))
                .valueExtractor(input -> input.get("identificatie"))
                .build();

            var nestedProperties = nestedSourcePaths.stream()
                .map(sourcePath -> new SelectedProperty(originType.getProperty(sourcePath.getFirstSegment())))
                .toList();

            nextOperations.put(propertyName, CollectionFetchOperation.builder()
                .source(sources.get(sourceAlias))
                .objectType(originType)
                .filter(filter)
                .selectedProperties(nestedProperties)
                .single(true)
                .build());

            return;
          }

          // TODO: Differing model aliases & type safety
          var nestedObjectType = modelMapping.getSourceModel(sourceAlias)
              .getObjectType(((Relation) property).getTarget());

          selectedProperties.add(new SelectedProperty(property, selectIdentity(nestedObjectType)));
          nextOperations.put(propertyName, fetchSourceObject(nestedObjectType, nestedSourcePaths, sourceAlias, false,
              inputMapper(propertyName)));
        });

    if (isCollection) {
      return CollectionFetchOperation.builder()
          .source(sources.get(sourceAlias))
          .objectType(sourceType)
          .selectedProperties(unmodifiableList(selectedProperties))
          .inputMapper(inputMapper)
          .nextOperations(unmodifiableMap(nextOperations))
          .build();
    }

    return ObjectFetchOperation.builder()
        .source(sources.get(sourceAlias))
        .objectType(sourceType)
        .selectedProperties(unmodifiableList(selectedProperties))
        .inputMapper(inputMapper)
        .keyExtractor(keyExtractor(sourceType))
        .nextOperations(unmodifiableMap(nextOperations))
        .build();
  }

  private Map<String, Object> mapResult(Map<Property, PropertyMapping> propertyMappings, Map<String, Object> result) {
    return propertyMappings.entrySet()
        .stream()
        .collect(HashMap::new, (acc, entry) -> acc.put(entry.getKey().getName(), mapPropertyResult(entry.getValue(),
            result)), HashMap::putAll);
  }

  private Object mapPropertyResult(PropertyMapping propertyMapping, Map<String, Object> result) {
    return propertyMapping.getPathMappings()
        .stream()
        .reduce(null, (previousValue, pathMapping) -> {
          var pathValue = pathMapping.getPaths()
              .stream()
              .map(path -> pathValue(result, path))
              .filter(Objects::nonNull)
              .findFirst()
              .orElse(null);

          if (pathMapping.hasTransforms()) {
            pathValue = transform(pathValue, pathMapping.getTransforms());
          }

          if (pathMapping.hasCombiner()) {
            pathValue = pathMapping.getCombiner()
                .apply(pathValue, previousValue);
          }

          return pathValue;
        }, noopCombiner());
  }

  private Object transform(Object value, List<Transform> transforms) {
    return transforms.stream()
        .reduce(value, (acc, transform) -> transform.apply(acc), noopCombiner());
  }
}
