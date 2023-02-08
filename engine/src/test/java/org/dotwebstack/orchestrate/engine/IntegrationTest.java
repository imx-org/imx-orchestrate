package org.dotwebstack.orchestrate.engine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.dotwebstack.orchestrate.engine.TestFixtures.createModelMapping;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import graphql.GraphQL;
import graphql.schema.idl.SchemaPrinter;
import java.util.List;
import java.util.Map;
import org.dotwebstack.orchestrate.engine.schema.SchemaFactory;
import org.dotwebstack.orchestrate.source.CollectionRequest;
import org.dotwebstack.orchestrate.source.DataRepository;
import org.dotwebstack.orchestrate.source.ObjectRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class IntegrationTest {

  @Mock
  private DataRepository dataRepositoryStub;

  @Test
  void queryObject_withoutEagerLoading() {
    when(dataRepositoryStub.findOne(any(ObjectRequest.class)))
        .thenAnswer(invocation -> {
          var objectRequest = (ObjectRequest) invocation.getArgument(0);
          var objectType = objectRequest.getObjectType();
          var objectkey = objectRequest.getObjectKey();

          return switch (objectType.getName()) {
            case "Nummeraanduiding":
              assertThat(objectRequest.getSelectedProperties()).hasSize(7);
              assertThat(objectkey).isEqualTo(Map.of("identificatie", "0200200000075716"));
              yield Mono.just(Map.of("identificatie", "0200200000075716", "huisnummer", 701, "postcode", "7334DP",
                  "ligtAan", Map.of("identificatie", "0200300022472362")));
            case "OpenbareRuimte":
              assertThat(objectRequest.getSelectedProperties()).hasSize(2);
              assertThat(objectkey).isEqualTo(Map.of("identificatie", "0200300022472362"));
              yield Mono.just(Map.of("naam", "Laan van Westenenk", "ligtIn", Map.of("identificatie", "3560")));
            case "Woonplaats":
              assertThat(objectRequest.getSelectedProperties()).hasSize(1);
              assertThat(objectkey).isEqualTo(Map.of("identificatie", "3560"));
              yield Mono.just(Map.of("naam", "Apeldoorn"));
            default:
              yield Mono.error(() -> new RuntimeException("Error!"));
          };
        });

    when(dataRepositoryStub.find(any(CollectionRequest.class)))
        .thenAnswer(invocation -> {
          var collectionRequest = (CollectionRequest) invocation.getArgument(0);
          var objectType = collectionRequest.getObjectType();

          return switch (objectType.getName()) {
            case "Verblijfsobject":
              assertThat(collectionRequest.getSelectedProperties()).hasSize(1);
              yield Flux.just(Map.of("identificatie", "0200010000130331"));
            default:
              yield Flux.error(() -> new RuntimeException("Error!"));
          };
        });

    var orchestration = Orchestration.builder()
        .modelMapping(createModelMapping())
        .source("bag", () -> dataRepositoryStub)
        .build();

    var schema = SchemaFactory.create(orchestration);
    System.out.println(new SchemaPrinter().print(schema));

    var graphQL = GraphQL.newGraphQL(schema)
        .build();

    var result = graphQL.execute("""
          query {
            adres(identificatie: "0200200000075716") {
              identificatie
              huisnummer
              huisletter
              huisnummertoevoeging
              postcode
              straatnaam
              plaatsnaam
              isHoofdadres
              omschrijving
            }
          }
        """);

    verify(dataRepositoryStub, times(3)).findOne(any(ObjectRequest.class));

    assertThat(result).isNotNull();
    assertThat(result.getErrors()).isEmpty();
    assertThat(result.isDataPresent()).isTrue();

    Map<String, Map<String, Object>> data = result.getData();
    Map<String, Object> adres = data.get("adres");

    assertThat(adres).isNotNull()
        .containsEntry("identificatie", "0200200000075716")
        .containsEntry("huisnummer", 701)
        .containsEntry("huisletter", null)
        .containsEntry("huisnummertoevoeging", null)
        .containsEntry("postcode", "7334DP")
        .containsEntry("straatnaam", "Laan van Westenenk")
        .containsEntry("plaatsnaam", "Apeldoorn")
        .containsEntry("isHoofdadres", true)
        .containsEntry("omschrijving", "Laan van Westenenk 701");

    System.out.println(result);
  }

  @Test
  void queryCollection_withoutEagerLoading() {
    when(dataRepositoryStub.findOne(any(ObjectRequest.class)))
        .thenAnswer(invocation -> {
          var objectRequest = (ObjectRequest) invocation.getArgument(0);
          var objectType = objectRequest.getObjectType();
          var objectkey = objectRequest.getObjectKey();

          return switch (objectType.getName()) {
            case "OpenbareRuimte":
              assertThat(objectRequest.getSelectedProperties()).hasSize(2);
              assertThat(objectkey).isEqualTo(Map.of("identificatie", "0200300022472362"));
              yield Mono.just(Map.of("naam", "Laan van Westenenk", "ligtIn", Map.of("identificatie", "3560")));
            case "Woonplaats":
              assertThat(objectRequest.getSelectedProperties()).hasSize(1);
              assertThat(objectkey).isEqualTo(Map.of("identificatie", "3560"));
              yield Mono.just(Map.of("naam", "Apeldoorn"));
            default:
              yield Mono.error(() -> new RuntimeException("Error!"));
          };
        });

    when(dataRepositoryStub.find(any(CollectionRequest.class)))
        .thenAnswer(invocation -> {
          var collectionRequest = (CollectionRequest) invocation.getArgument(0);
          var objectType = collectionRequest.getObjectType();

          return switch (objectType.getName()) {
            case "Nummeraanduiding":
              assertThat(collectionRequest.getSelectedProperties()).hasSize(7);
              yield Flux.just(Map.of("identificatie", "0200200000075716", "huisnummer", 701, "postcode", "7334DP",
                      "ligtAan", Map.of("identificatie", "0200300022472362")),
                  Map.of("identificatie", "0200200000075717", "huisnummer", 702, "postcode", "7334DP", "ligtAan",
                      Map.of("identificatie", "0200300022472362")),
                  Map.of("identificatie", "0200200000075718", "huisnummer", 703, "postcode", "7334DP", "ligtAan",
                      Map.of("identificatie", "0200300022472362")));
            default:
              yield Mono.error(() -> new RuntimeException("Error!"));
          };
        });

    var orchestration = Orchestration.builder()
        .modelMapping(createModelMapping())
        .source("bag", () -> dataRepositoryStub)
        .build();

    var schema = SchemaFactory.create(orchestration);
    System.out.println(new SchemaPrinter().print(schema));

    var graphQL = GraphQL.newGraphQL(schema)
        .build();

    var result = graphQL.execute("""
          query {
            adresCollection {
              identificatie
              huisnummer
              huisletter
              huisnummertoevoeging
              postcode
              straatnaam
              plaatsnaam
              isHoofdadres
              omschrijving
            }
          }
        """);

    verify(dataRepositoryStub, times(1)).find(any(CollectionRequest.class));
    verify(dataRepositoryStub, times(6)).findOne(any(ObjectRequest.class));

    assertThat(result).isNotNull();
    assertThat(result.getErrors()).isEmpty();
    assertThat(result.isDataPresent()).isTrue();

    Map<String, Object> data = result.getData();
    assertThat(data).containsAllEntriesOf(Map.of("adresCollection", List.of(
        Map.of("identificatie", "0200200000075716", "huisnummer", 701, "postcode", "7334DP", "straatnaam", "Laan van " +
            "Westenenk", "plaatsnaam", "Apeldoorn"),
        Map.of("identificatie", "0200200000075717", "huisnummer", 702, "postcode", "7334DP", "straatnaam", "Laan van " +
            "Westenenk", "plaatsnaam", "Apeldoorn"),
        Map.of("identificatie", "0200200000075718", "huisnummer", 703, "postcode", "7334DP", "straatnaam", "Laan van " +
            "Westenenk", "plaatsnaam", "Apeldoorn"))));

    System.out.println(result);
  }
}
