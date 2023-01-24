package org.dotwebstack.orchestrate.engine.fetch;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLObjectType;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.dotwebstack.orchestrate.engine.OrchestrateException;
import org.dotwebstack.orchestrate.model.ModelMapping;
import org.dotwebstack.orchestrate.model.types.Field;
import org.dotwebstack.orchestrate.model.types.ObjectType;
import org.dotwebstack.orchestrate.source.ObjectRequest;
import org.dotwebstack.orchestrate.source.SelectedField;
import org.dotwebstack.orchestrate.source.Source;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class ObjectFetcher implements DataFetcher<Mono<Map<String, Object>>> {

  private final ModelMapping modelMapping;

  private final Map<String, Source> sourceMap;

  @Override
  public Mono<Map<String, Object>> get(DataFetchingEnvironment environment) {
    var graphQLOutputType = environment.getFieldType();

    if (graphQLOutputType instanceof GraphQLObjectType) {
      return handleObjectType(Map.of("identificatie", "0200200000075716"));
    }

    throw new OrchestrateException("The object fetcher only supports object types, no unions or interfaces (yet).");
  }

  private Mono<Map<String, Object>> handleObjectType(Map<String, Object> objectKey) {
    var sourceModel = modelMapping.getSourceModel("bag")
        .orElseThrow();

    var oprKeyExtractor = keyExtractor(sourceModel.getObjectType("OpenbareRuimte")
        .orElseThrow());

    var wplKeyExtractor = keyExtractor(sourceModel.getObjectType("Woonplaats")
        .orElseThrow());

    return Mono.just(objectKey)
        .flatMap(this::retrieveNum)
        .flatMap(numResult -> retrieveOpr(oprKeyExtractor.apply(numResult))
            .flatMap(oprResult -> retrieveWpl(wplKeyExtractor.apply(oprResult))
                .map(wplResult -> mapResult(numResult, oprResult, wplResult))));
  }

  private Mono<Map<String, Object>> retrieveNum(Map<String, Object> objectKey) {
    var source = sourceMap.get("bag");

    var sourceModel = modelMapping.getSourceModel("bag")
        .orElseThrow();

    var numSourceType = sourceModel.getObjectType("Nummeraanduiding")
        .orElseThrow();

    var oprSourceType = sourceModel.getObjectType("OpenbareRuimte")
        .orElseThrow();

    var numObjectRequest = ObjectRequest.builder()
        .objectType(numSourceType)
        .objectKey(objectKey)
        .selectedFields(List.of(
            SelectedField.builder()
                .field(numSourceType.getField("identificatie")
                    .orElseThrow())
                .build(),
            SelectedField.builder()
                .field(numSourceType.getField("huisnummer")
                    .orElseThrow())
                .build(),
            SelectedField.builder()
                .field(numSourceType.getField("postcode")
                    .orElseThrow())
                .build(),
            SelectedField.builder()
                .field(numSourceType.getField("ligtAan")
                    .orElseThrow())
                .selectedFields(List.of(
                    SelectedField.builder()
                        .field(oprSourceType.getField("identificatie")
                            .orElseThrow())
                        .build()))
                .build()))
        .build();

    return source.getDataRepository()
        .findOne(numObjectRequest)
        .log(numSourceType.getName());
  }

  private Mono<Map<String, Object>> retrieveOpr(Map<String, Object> objectKey) {
    var source = sourceMap.get("bag");

    var sourceModel = modelMapping.getSourceModel("bag")
        .orElseThrow();

    var oprSourceType = sourceModel.getObjectType("OpenbareRuimte")
        .orElseThrow();

    var wplSourceType = sourceModel.getObjectType("Woonplaats")
        .orElseThrow();

    var objectRequest = ObjectRequest.builder()
        .objectType(oprSourceType)
        .objectKey(objectKey)
        .selectedFields(List.of(
            SelectedField.builder()
                .field(oprSourceType.getField("identificatie")
                    .orElseThrow())
                .build(),
            SelectedField.builder()
                .field(oprSourceType.getField("naam")
                    .orElseThrow())
                .build(),
            SelectedField.builder()
                .field(oprSourceType.getField("ligtIn")
                    .orElseThrow())
                .selectedFields(List.of(
                    SelectedField.builder()
                        .field(wplSourceType.getField("identificatie")
                            .orElseThrow())
                        .build()))
                .build()))
        .build();

    return source.getDataRepository()
        .findOne(objectRequest)
        .log(oprSourceType.getName());
  }

  private Mono<Map<String, Object>> retrieveWpl(Map<String, Object> objectKey) {
    var source = sourceMap.get("bag");

    var sourceModel = modelMapping.getSourceModel("bag")
        .orElseThrow();

    var wplSourceType = sourceModel.getObjectType("Woonplaats")
        .orElseThrow();

    var objectRequest = ObjectRequest.builder()
        .objectType(wplSourceType)
        .objectKey(objectKey)
        .selectedFields(List.of(
            SelectedField.builder()
                .field(wplSourceType.getField("identificatie")
                    .orElseThrow())
                .build(),
            SelectedField.builder()
                .field(wplSourceType.getField("naam")
                    .orElseThrow())
                .build()))
        .build();

    return source.getDataRepository()
        .findOne(objectRequest)
        .log(wplSourceType.getName());
  }

  private Map<String, Object> mapResult(Map<String, Object> numResult, Map<String, Object> oprResult, Map<String,
      Object> wplResult) {
    return Map.of(
        "identificatie", numResult.get("identificatie"),
        "huisnummer", numResult.get("huisnummer"),
        "postcode", numResult.get("postcode"),
        "straatnaam", oprResult.get("naam"),
        "plaatsnaam", wplResult.get("naam"));
  }

  private static UnaryOperator<Map<String, Object>> keyExtractor(ObjectType objectType) {
    return data -> objectType.getIdentityFields()
        .stream()
        .collect(Collectors.toMap(Field::getName, field -> data.get(field.getName())));
  }
}
