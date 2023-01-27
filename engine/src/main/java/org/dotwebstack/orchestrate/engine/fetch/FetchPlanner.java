package org.dotwebstack.orchestrate.engine.fetch;

import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;
import static org.dotwebstack.orchestrate.engine.fetch.FetchUtils.keyExtractor;
import static org.dotwebstack.orchestrate.engine.fetch.FetchUtils.selectIdentifyFields;

import graphql.schema.DataFetchingEnvironment;
import graphql.schema.DataFetchingFieldSelectionSet;
import graphql.schema.GraphQLObjectType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;
import lombok.RequiredArgsConstructor;
import org.dotwebstack.orchestrate.model.FieldPath;
import org.dotwebstack.orchestrate.model.ModelMapping;
import org.dotwebstack.orchestrate.model.SourceTypeRef;
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

    return fetchTargetObject(targetType, environment.getSelectionSet())
        .execute(environment.getArguments());
  }

  private FetchOperation fetchTargetObject(ObjectType targetType, DataFetchingFieldSelectionSet selectionSet) {
    var targetMapping = modelMapping.getObjectTypeMappings()
        .get(targetType.getName());

    var sourcePaths = selectionSet.getImmediateFields()
        .stream()
        .map(field -> targetType.getField(field.getName()))
        .flatMap(field -> targetMapping.getFieldMapping(field.getName())
            .getSourcePaths()
            .stream())
        .toList();

    return fetchSourceObject(targetMapping.getSourceRoot(), sourcePaths);
  }

  private FetchOperation fetchSourceObject(SourceTypeRef sourceTypeRef, List<FieldPath> sourcePaths) {
    var sourceType = modelMapping.getSourceModels()
        .get(sourceTypeRef.getModelAlias())
        .getObjectType(sourceTypeRef.getObjectType());

    return fetchSourceObject(sourceType, sourcePaths, sourceTypeRef.getModelAlias());
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

    var resultMapper = UnaryOperator.<Map<String, Object>>identity();

    if (sourceType.getName().equals("Nummeraanduiding")) {
      resultMapper = result -> {
        var ligtAan = (Map<String, Object>) result.get("ligtAan");
        var ligtIn = (Map<String, Object>) ligtAan.get("ligtIn");

        return Map.of(
            "identificatie", result.get("identificatie"),
            "huisnummer", result.get("huisnummer"),
            "postcode", result.get("postcode"),
            "straatnaam", ligtAan.get("naam"),
            "plaatsnaam", ligtIn.get("naam"));
      };
    }

    return FetchOperation.builder()
        .source(sources.get(sourceAlias))
        .objectType(sourceType)
        .selectedFields(unmodifiableList(selectedFields))
        .keyExtractor(keyExtractor(sourceType))
        .resultMapper(resultMapper)
        .nextOperations(unmodifiableMap(nextOperations))
        .build();
  }
}
