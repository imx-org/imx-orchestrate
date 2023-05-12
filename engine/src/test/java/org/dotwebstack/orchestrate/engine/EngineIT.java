package org.dotwebstack.orchestrate.engine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.list;
import static org.assertj.core.api.InstanceOfAssertFactories.map;
import static org.dotwebstack.orchestrate.engine.TestFixtures.createBagModel;
import static org.dotwebstack.orchestrate.engine.TestFixtures.createBgtModel;
import static org.dotwebstack.orchestrate.engine.TestFixtures.createModelMapping;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import graphql.GraphQL;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import org.dotwebstack.orchestrate.engine.schema.SchemaConstants;
import org.dotwebstack.orchestrate.engine.schema.SchemaFactory;
import org.dotwebstack.orchestrate.model.ObjectType;
import org.dotwebstack.orchestrate.model.Path;
import org.dotwebstack.orchestrate.source.BatchRequest;
import org.dotwebstack.orchestrate.source.CollectionRequest;
import org.dotwebstack.orchestrate.source.DataRepository;
import org.dotwebstack.orchestrate.source.ObjectRequest;
import org.dotwebstack.orchestrate.source.file.FileSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class EngineIT {

  private DataRepository bagRepository;

  private DataRepository bgtRepository;

  @Mock
  private DataRepository bagRepositoryStub;

  @Mock
  private DataRepository bgtRepositoryStub;

  private GraphQL graphQL;

  @BeforeEach
  void setUp() {
    bagRepository = new FileSource(createBagModel(), Paths.get("../data/bag")).getDataRepository();
    bgtRepository = new FileSource(createBgtModel(), Paths.get("../data/bgt")).getDataRepository();

    var orchestration = Orchestration.builder()
        .modelMapping(createModelMapping())
        .source("bag", () -> bagRepositoryStub)
        .source("bgt", () -> bgtRepositoryStub)
        .build();

    graphQL = GraphQL.newGraphQL(SchemaFactory.create(orchestration))
        .build();
  }

  @Test
  void queryObject_withCrossModelRelation() {
    when(bgtRepositoryStub.findOne(any(ObjectRequest.class)))
        .thenAnswer(invocation -> bgtRepository.findOne(invocation.getArgument(0)));

    when(bagRepositoryStub.findOne(any(ObjectRequest.class)))
        .thenAnswer(invocation -> bagRepository.findOne(invocation.getArgument(0)));

    when(bagRepositoryStub.find(any(CollectionRequest.class)))
        .thenAnswer(invocation -> bagRepository.find(invocation.getArgument(0)));

    var result = graphQL.execute("""
          query {
            gebouw(identificatie: "G0200.42b3d39246840268e0530a0a28492340") {
              identificatie
              bouwjaar
              heeftAlsAdres {
                identificatie
                postcode
                huisnummer
              }
            }
          }
        """);

    Map<String, Object> data = result.getData();

    assertThat(result.getErrors()).isEmpty();
    assertThat(data).isNotNull()
        .hasEntrySatisfying("gebouw", gebouw ->
            assertThat(gebouw).isNotNull()
                .isInstanceOf(Map.class)
                .asInstanceOf(map(String.class, Object.class))
                .containsEntry("identificatie", "G0200.42b3d39246840268e0530a0a28492340")
                .containsEntry("bouwjaar", "2006")
                .hasEntrySatisfying("heeftAlsAdres", heeftAlsAdres ->
                    assertThat(heeftAlsAdres).isNotNull()
                        .isInstanceOf(List.class)
                        .asInstanceOf(list(Map.class))
                        .hasSize(2)));
  }

  @Test
  @SuppressWarnings("unchecked")
  void queryObject_withoutBatchLoading() {
    when(bagRepositoryStub.findOne(any(ObjectRequest.class)))
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

    when(bagRepositoryStub.find(any(CollectionRequest.class)))
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

    verify(bagRepositoryStub, times(3)).findOne(any(ObjectRequest.class));

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
    var orchestratedProperties = ((List<Map<String, Object>>) lineage.get("orchestratedProperties"));

    assertThat(orchestratedProperties).isNotNull()
        .hasSize(7)
        .filteredOn("property", "identificatie")
        .hasSize(1)
        .first()
        .satisfies(orchestratedProperty -> {
          var sourceProperty = ((List<Map<String, Object>>) orchestratedProperty.get("isDerivedFrom")).get(0);
          assertThat(sourceProperty).isNotNull()
              .containsEntry("property", "identificatie")
              .containsEntry("subject", Map.of("objectType", "Nummeraanduiding", "objectKey", "0200200000075716"));
        });
  }

  @Test
  @SuppressWarnings("unchecked")
  void queryObject_withBatchLoading() {
    when(bgtRepositoryStub.find(any(CollectionRequest.class)))
        .thenAnswer(invocation -> {
          var collectionRequest = (CollectionRequest) invocation.getArgument(0);
          var objectType = collectionRequest.getObjectType();

          return switch (objectType.getName()) {
            case "Pand":
              assertThat(collectionRequest.getSelectedProperties()).hasSize(2);
              yield bgtRepository.find(collectionRequest);
            default:
              yield Flux.error(() -> new RuntimeException("Error!"));
          };
        });

    when(bagRepositoryStub.supportsBatchLoading(any(ObjectType.class))).thenReturn(true);
    when(bagRepositoryStub.findBatch(any(BatchRequest.class)))
        .thenAnswer(invocation -> {
          var batchRequest = (BatchRequest) invocation.getArgument(0);
          var objectType = batchRequest.getObjectType();
          var objectkeys = batchRequest.getObjectKeys();

          return switch (objectType.getName()) {
            case "Pand":
              assertThat(batchRequest.getSelectedProperties()).hasSize(2);
              assertThat(objectkeys).hasSize(3)
                  .contains(Map.of("identificatie", "0200100000085932"))
                  .contains(Map.of("identificatie", "0050100000356176"))
                  .contains(Map.of("identificatie", "0000000000000000"));
              yield bagRepository.findBatch(batchRequest);
            default:
              yield Flux.error(() -> new RuntimeException("Error!"));
          };
        });

    var result = graphQL.execute("""
          query {
            gebouwCollection {
              identificatie
              bouwjaar
            }
          }
        """);

    verify(bgtRepositoryStub, times(1)).find(any(CollectionRequest.class));
    verify(bagRepositoryStub, times(1)).supportsBatchLoading(any(ObjectType.class));
    verify(bagRepositoryStub, times(1)).findBatch(any(BatchRequest.class));

    assertThat(result).isNotNull();
    assertThat(result.getErrors()).isEmpty();
    assertThat(result.isDataPresent()).isTrue();

    Map<String, List<Map<String, Object>>> data = result.getData();
    var gebouwCollection = data.get("gebouwCollection");

    assertThat(gebouwCollection).isNotNull()
        .hasSize(3);

    assertThat(gebouwCollection.get(0)).isNotNull()
        .containsEntry("identificatie", "G0200.42b3d39246840268e0530a0a28492340")
        .containsEntry("bouwjaar", "2006");

    assertThat(gebouwCollection.get(1)).isNotNull()
        .containsEntry("identificatie", "G0050.19cb5132f8b23142e053440a0c0a9802")
        .containsEntry("bouwjaar", "1997");

    assertThat(gebouwCollection.get(2)).isNotNull()
        .containsEntry("identificatie", "G0050.5044e42a2f0f233fe053440a0c0a31e0")
        .containsEntry("bouwjaar", null);
  }

  @Test
  @SuppressWarnings("unchecked")
  void queryCollection_withoutBatchLoading() {
    when(bagRepositoryStub.findOne(any(ObjectRequest.class)))
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

    when(bagRepositoryStub.find(any(CollectionRequest.class)))
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
              assertThat(filter.getPath()).isEqualTo(Path.fromString("heeftAlsHoofdadres"));
              var filterValue = (Map<String, Object>) filter.getValue();
              assertThat(filterValue).isNotNull()
                  .containsKey("identificatie");
              yield bagRepository.find(collectionRequest);
            default:
              yield Flux.error(() -> new RuntimeException("Error!"));
          };
        });

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

    verify(bagRepositoryStub, times(4)).find(any(CollectionRequest.class));
    verify(bagRepositoryStub, times(3)).findOne(any(ObjectRequest.class));

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
