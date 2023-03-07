package org.dotwebstack.orchestrate.engine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.dotwebstack.orchestrate.engine.TestFixtures.createBagModel;
import static org.dotwebstack.orchestrate.engine.TestFixtures.createBgtModel;
import static org.dotwebstack.orchestrate.engine.TestFixtures.createModelMapping;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import graphql.GraphQL;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.dotwebstack.orchestrate.engine.schema.SchemaConstants;
import org.dotwebstack.orchestrate.engine.schema.SchemaFactory;
import org.dotwebstack.orchestrate.model.PropertyPath;
import org.dotwebstack.orchestrate.source.CollectionRequest;
import org.dotwebstack.orchestrate.source.DataRepository;
import org.dotwebstack.orchestrate.source.ObjectRequest;
import org.dotwebstack.orchestrate.source.Source;
import org.dotwebstack.orchestrate.source.file.FileSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class EngineIT {

  private static final Map<String, Source> SOURCE_MAP = new HashMap<>();

  @Mock
  private DataRepository dataRepositoryStub;

  @BeforeAll
  static void beforeAll() {
    SOURCE_MAP.put("bag", new FileSource(createBagModel(), Paths.get("data/bag")));
    SOURCE_MAP.put("bgt", new FileSource(createBgtModel(), Paths.get("data/bgt")));
  }

  @Test
  void queryObject_withCrossModelRelation() {
    var orchestration = Orchestration.builder()
        .modelMapping(createModelMapping())
        .sources(SOURCE_MAP)
        .build();

    var graphQL = GraphQL.newGraphQL(SchemaFactory.create(orchestration))
        .build();

    var result = graphQL.execute("""
          query {
            gebouw(identificatie: "G0200.42b3d39246840268e0530a0a28492340") {
              identificatie
              bouwjaar
            }
          }
        """);

    Map<String, Map<String, Object>> data = result.getData();
    assertThat(data).isNotNull()
        .containsKey("gebouw");
    assertThat(data.get("gebouw")).isNotNull()
        .containsEntry("identificatie", "G0200.42b3d39246840268e0530a0a28492340")
        .containsEntry("bouwjaar", "2006");
  }

  @Test
  @SuppressWarnings("unchecked")
  void queryObject_withoutEagerLoading() {
    var bagRepository = SOURCE_MAP.get("bag")
        .getDataRepository();

    when(dataRepositoryStub.findOne(any(ObjectRequest.class)))
        .thenAnswer(invocation -> {
          var objectRequest = (ObjectRequest) invocation.getArgument(0);
          var objectType = objectRequest.getObjectType();
          var objectkey = objectRequest.getObjectKey();

          return switch (objectType.getName()) {
            case "Nummeraanduiding":
              assertThat(objectRequest.getSelectedProperties()).hasSize(7);
              assertThat(objectkey).isEqualTo(Map.of("identificatie", "0200200000075716"));
              yield bagRepository.findOne(objectRequest);
            case "OpenbareRuimte":
              assertThat(objectRequest.getSelectedProperties()).hasSize(3);
              assertThat(objectkey).isEqualTo(Map.of("identificatie", "0200300022472362"));
              yield bagRepository.findOne(objectRequest);
            case "Woonplaats":
              assertThat(objectRequest.getSelectedProperties()).hasSize(2);
              assertThat(objectkey).isEqualTo(Map.of("identificatie", "3560"));
              yield bagRepository.findOne(objectRequest);
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
              yield bagRepository.find(collectionRequest);
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
              hasLineage {
                orchestratedProperties {
                  property
                  isDerivedFrom {
                    property
                    subject {
                      objectType
                      objectKey
                    }
                  }
                }
              }
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
        .containsEntry("omschrijving", "Laan van Westenenk 701, 7334DP Apeldoorn");

    var lineage = (Map<String, Object>) adres.get(SchemaConstants.HAS_LINEAGE_FIELD);
    var orchestratedProperty = ((List<Map<String, Object>>) lineage.get("orchestratedProperties")).get(0);
    var sourceProperty = ((List<Map<String, Object>>) orchestratedProperty.get("isDerivedFrom")).get(0);

    assertThat(orchestratedProperty).isNotNull()
        .containsEntry("property", "identificatie");
    assertThat(sourceProperty).isNotNull()
        .containsEntry("property", "identificatie")
        .containsEntry("subject", Map.of("objectType", "Nummeraanduiding", "objectKey", "0200200000075716"));
  }

  @Test
  void queryCollection_withoutEagerLoading() {
    var bagRepository = SOURCE_MAP.get("bag")
        .getDataRepository();

    when(dataRepositoryStub.findOne(any(ObjectRequest.class)))
        .thenAnswer(invocation -> {
          var objectRequest = (ObjectRequest) invocation.getArgument(0);
          var objectType = objectRequest.getObjectType();
          var objectkey = objectRequest.getObjectKey();

          return switch (objectType.getName()) {
            case "OpenbareRuimte":
              assertThat(objectRequest.getSelectedProperties()).hasSize(3);
              assertThat(objectkey).isEqualTo(Map.of("identificatie", "0200300022472362"));
              yield bagRepository.findOne(objectRequest);
            case "Woonplaats":
              assertThat(objectRequest.getSelectedProperties()).hasSize(2);
              yield bagRepository.findOne(objectRequest);
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
              yield bagRepository.find(collectionRequest);
            case "Verblijfsobject":
              assertThat(collectionRequest.getSelectedProperties()).hasSize(1);
              var filter = collectionRequest.getFilter();
              assertThat(filter.getPropertyPath()).isEqualTo(PropertyPath.fromString("heeftAlsHoofdadres" +
                  "/identificatie"));
              assertThat(filter.getValue()).isInstanceOf(String.class);
              yield bagRepository.find(collectionRequest);
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
