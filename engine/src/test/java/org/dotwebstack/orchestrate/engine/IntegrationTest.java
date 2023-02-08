package org.dotwebstack.orchestrate.engine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.dotwebstack.orchestrate.engine.TestFixtures.NUM_DATA;
import static org.dotwebstack.orchestrate.engine.TestFixtures.OPR_DATA;
import static org.dotwebstack.orchestrate.engine.TestFixtures.VBO_DATA;
import static org.dotwebstack.orchestrate.engine.TestFixtures.WPL_DATA;
import static org.dotwebstack.orchestrate.engine.TestFixtures.createModelMapping;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import graphql.GraphQL;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.dotwebstack.orchestrate.engine.schema.SchemaFactory;
import org.dotwebstack.orchestrate.model.PropertyPath;
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
              yield Mono.just(NUM_DATA.get("0200200000075716"));
            case "OpenbareRuimte":
              assertThat(objectRequest.getSelectedProperties()).hasSize(2);
              assertThat(objectkey).isEqualTo(Map.of("identificatie", "0200300022472362"));
              yield Mono.just(OPR_DATA.get("0200300022472362"));
            case "Woonplaats":
              assertThat(objectRequest.getSelectedProperties()).hasSize(1);
              assertThat(objectkey).isEqualTo(Map.of("identificatie", "3560"));
              yield Mono.just(WPL_DATA.get("3560"));
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

    var graphQL = GraphQL.newGraphQL(SchemaFactory.create(orchestration))
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
              yield Mono.just(OPR_DATA.get("0200300022472362"));
            case "Woonplaats":
              assertThat(objectRequest.getSelectedProperties()).hasSize(1);
              yield Mono.just(WPL_DATA.get((String) objectkey.get("identificatie")));
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
              yield Flux.fromIterable(NUM_DATA.values());
            case "Verblijfsobject":
              assertThat(collectionRequest.getSelectedProperties()).hasSize(1);
              var filter = collectionRequest.getFilter();
              assertThat(filter.getPropertyPath()).isEqualTo(PropertyPath.fromString("isHoofdadresVan/identificatie"));
              assertThat(filter.getValue()).isInstanceOf(String.class);
              yield Optional.ofNullable(VBO_DATA.get((String) filter.getValue()))
                  .map(Flux::just)
                  .orElse(Flux.empty());
            default:
              yield Mono.error(() -> new RuntimeException("Error!"));
          };
        });

    var orchestration = Orchestration.builder()
        .modelMapping(createModelMapping())
        .source("bag", () -> dataRepositoryStub)
        .build();

    var graphQL = GraphQL.newGraphQL(SchemaFactory.create(orchestration))
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

    verify(dataRepositoryStub, times(4)).find(any(CollectionRequest.class));
    verify(dataRepositoryStub, times(7)).findOne(any(ObjectRequest.class));

    assertThat(result).isNotNull();
    assertThat(result.getErrors()).isEmpty();
    assertThat(result.isDataPresent()).isTrue();

    Map<String, List<Map<String, Object>>> data = result.getData();
    var adresCollection = data.get("adresCollection");
    assertThat(adresCollection).hasSize(3);

    assertThat(adresCollection.get(0)).isNotNull()
        .containsEntry("identificatie", "0200200000075716")
        .containsEntry("huisnummer", 701)
        .containsEntry("huisletter", null)
        .containsEntry("huisnummertoevoeging", null)
        .containsEntry("postcode", "7334DP")
        .containsEntry("straatnaam", "Laan van Westenenk")
        .containsEntry("plaatsnaam", "Apeldoorn")
        .containsEntry("isHoofdadres", true)
        .containsEntry("omschrijving", "Laan van Westenenk 701, 7334DP Apeldoorn");

    assertThat(adresCollection.get(1)).isNotNull()
        .containsEntry("identificatie", "0200200000075717")
        .containsEntry("huisnummer", 702)
        .containsEntry("huisletter", null)
        .containsEntry("huisnummertoevoeging", null)
        .containsEntry("postcode", "7334DP")
        .containsEntry("straatnaam", "Laan van Westenenk")
        .containsEntry("plaatsnaam", "Apeldoorn")
        .containsEntry("isHoofdadres", false)
        .containsEntry("omschrijving", "Laan van Westenenk 702, 7334DP Apeldoorn");

    assertThat(adresCollection.get(2)).isNotNull()
        .containsEntry("identificatie", "0200200000075718")
        .containsEntry("huisnummer", 703)
        .containsEntry("huisletter", "C")
        .containsEntry("huisnummertoevoeging", "8")
        .containsEntry("postcode", "7334DP")
        .containsEntry("straatnaam", "Laan van Westenenk")
        .containsEntry("plaatsnaam", "Beekbergen")
        .containsEntry("isHoofdadres", true)
        .containsEntry("omschrijving", "Laan van Westenenk 703 C-8, 7334DP Beekbergen");
  }
}
