package org.dotwebstack.orchestrate.engine.fetch;

import static org.dotwebstack.orchestrate.engine.fetch.FetchUtils.keyExtractor;
import static org.dotwebstack.orchestrate.engine.fetch.FetchUtils.selectFields;

import graphql.schema.DataFetchingEnvironment;
import graphql.schema.DataFetchingFieldSelectionSet;
import graphql.schema.GraphQLObjectType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.dotwebstack.orchestrate.model.FieldPath;
import org.dotwebstack.orchestrate.model.ModelMapping;
import org.dotwebstack.orchestrate.model.SourceTypeRef;
import org.dotwebstack.orchestrate.model.types.ObjectType;
import org.dotwebstack.orchestrate.source.Source;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public final class FetchPlanner {

  private final ModelMapping modelMapping;

  private final Map<String, Source> sources;

  public Mono<Map<String, Object>> fetch(DataFetchingEnvironment environment, GraphQLObjectType outputType) {
    var targetType = modelMapping.getTargetModel()
        .getObjectType(outputType.getName());

    // TODO: Extract from arguments
    var objectKey = Map.<String, Object>of("identificatie", "0200200000075716");

    return fetchTargetObject(targetType, environment.getSelectionSet())
        .execute(objectKey);
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
    var source = sources.get(sourceTypeRef.getModelAlias());

    var sourceType = modelMapping.getSourceModels()
        .get(sourceTypeRef.getModelAlias())
        .getObjectType(sourceTypeRef.getObjectType());

    // TODO: Make dynamic
    // TODO: Extract keys from nested object
    var nestedSelections = new HashMap<String, FetchOperation>();

    if (sourceType.getName().equals("Nummeraanduiding")) {
      nestedSelections.put("ligtAan", fetchSourceObject(SourceTypeRef.fromString("bag:OpenbareRuimte"),
          List.of(FieldPath.fromString("naam"), FieldPath.fromString("ligtIn/naam"))));
    }

    if (sourceType.getName().equals("OpenbareRuimte")) {
      nestedSelections.put("ligtIn", fetchSourceObject(SourceTypeRef.fromString("bag:Woonplaats"),
          List.of(FieldPath.fromString("naam"))));
    }

    return FetchOperation.builder()
        .source(source)
        .objectType(sourceType)
        .selectedFields(selectFields(sourceType, sourcePaths))
        .objectKeyExtractor(keyExtractor(sourceType))
        .nextOperations(nestedSelections)
        .build();
  }
}
