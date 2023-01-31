package org.dotwebstack.orchestrate.engine.fetch;

import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.dotwebstack.orchestrate.engine.fetch.FetchUtils.keyExtractor;
import static org.dotwebstack.orchestrate.engine.fetch.FetchUtils.selectIdentifyFields;

import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLObjectType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.dotwebstack.orchestrate.model.FieldMapping;
import org.dotwebstack.orchestrate.model.FieldPath;
import org.dotwebstack.orchestrate.model.ModelMapping;
import org.dotwebstack.orchestrate.model.types.Field;
import org.dotwebstack.orchestrate.model.types.ObjectType;
import org.dotwebstack.orchestrate.model.types.ObjectTypeRef;
import org.dotwebstack.orchestrate.source.SelectedField;
import org.dotwebstack.orchestrate.source.Source;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public final class FetchPlanner {

  private final ModelMapping modelMapping;

  private final Map<String, Source> sources;

  public Mono<Map<String, Object>> fetch(DataFetchingEnvironment environment, GraphQLObjectType outputType) {
    var targetType = modelMapping.getTargetModel()
        .getObjectType(outputType.getName());

    var targetMapping = modelMapping.getObjectTypeMappings()
        .get(targetType.getName());

    var fieldMappings = new HashMap<Field, FieldMapping>();
    var sourcePaths = new ArrayList<FieldPath>();

    environment.getSelectionSet()
        .getImmediateFields()
        .stream()
        .map(field -> targetType.getField(field.getName()))
        .forEach(field -> {
          var fieldMapping = targetMapping.getFieldMapping(field.getName());
          fieldMappings.put(field, fieldMapping);
          sourcePaths.addAll(fieldMapping.getSourcePaths());
        });

    var sourceRoot = targetMapping.getSourceRoot();

    var sourceType = modelMapping.getSourceModels()
        .get(sourceRoot.getModelAlias())
        .getObjectType(sourceRoot.getObjectType());

    return fetchSourceObject(sourceType, unmodifiableList(sourcePaths), sourceRoot.getModelAlias())
        .execute(environment.getArguments())
        .map(result -> mapResult(unmodifiableMap(fieldMappings), result));
  }

  private FetchOperation fetchSourceObject(ObjectType sourceType, List<FieldPath> sourcePaths, String sourceAlias) {
    var selectedFields = new ArrayList<SelectedField>();

    sourcePaths.stream()
        .filter(FieldPath::isLeaf)
        .map(sourcePath -> new SelectedField(sourceType.getField(sourcePath.getFirstSegment())))
        .forEach(selectedFields::add);

    var nextOperations = new HashMap<String, FetchOperation>();

    sourcePaths.stream()
        .filter(not(FieldPath::isLeaf))
        .collect(groupingBy(FieldPath::getFirstSegment, mapping(FieldPath::withoutFirstSegment, toList())))
        .forEach((fieldName, nestedSourcePaths) -> {
          var field = sourceType.getField(fieldName);

          // TODO: Differing model aliases & type safety
          var nestedObjectType = modelMapping.getSourceModel(sourceAlias)
              .getObjectType((ObjectTypeRef) field.getType());

          selectedFields.add(new SelectedField(field, selectIdentifyFields(nestedObjectType)));
          nextOperations.put(fieldName, fetchSourceObject(nestedObjectType, nestedSourcePaths, sourceAlias));
        });

    return FetchOperation.builder()
        .source(sources.get(sourceAlias))
        .objectType(sourceType)
        .selectedFields(unmodifiableList(selectedFields))
        .keyExtractor(keyExtractor(sourceType))
        .nextOperations(unmodifiableMap(nextOperations))
        .build();
  }

  private Map<String, Object> mapResult(Map<Field, FieldMapping> fieldMappings, Map<String, Object> result) {
    return fieldMappings.entrySet()
        .stream()
        .collect(toMap(entry -> entry.getKey().getName(), entry -> mapFieldResult(entry.getValue(), result)));
  }

  private Object mapFieldResult(FieldMapping fieldMapping, Map<String, Object> result) {
    return fieldMapping.getSourcePaths()
        .stream()
        .map(sourcePath -> mapFieldResult(sourcePath, result))
        .filter(Objects::nonNull)
        .findFirst();
  }

  private Object mapFieldResult(FieldPath sourcePath, Map<String, Object> result) {
    var firstSegment = sourcePath.getFirstSegment();

    if (sourcePath.isLeaf()) {
      return result.get(firstSegment);
    }

    @SuppressWarnings("unchecked")
    var fieldValue = (Map<String, Object>) result.get(firstSegment);

    if (fieldValue == null) {
      return null;
    }

    return mapFieldResult(sourcePath.withoutFirstSegment(), fieldValue);
  }
}
