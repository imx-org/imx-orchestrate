package nl.geostandaarden.imx.orchestrate.engine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.list;
import static org.assertj.core.api.InstanceOfAssertFactories.map;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import graphql.ExecutionResult;
import graphql.GraphQL;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import nl.geostandaarden.imx.orchestrate.engine.schema.SchemaFactory;
import nl.geostandaarden.imx.orchestrate.model.ComponentRegistry;
import nl.geostandaarden.imx.orchestrate.model.loader.ModelLoaderRegistry;
import nl.geostandaarden.imx.orchestrate.model.types.ValueTypeRegistry;
import nl.geostandaarden.imx.orchestrate.parser.yaml.YamlModelLoader;
import nl.geostandaarden.imx.orchestrate.parser.yaml.YamlModelMappingParser;
import nl.geostandaarden.imx.orchestrate.source.BatchRequest;
import nl.geostandaarden.imx.orchestrate.source.DataRepository;
import nl.geostandaarden.imx.orchestrate.source.ObjectRequest;
import nl.geostandaarden.imx.orchestrate.source.file.FileSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EngineIT {

  private DataRepository bldRepository;

  @Mock
  private DataRepository bldRepositoryStub;

  private GraphQL graphQL;

  @BeforeEach
  void setUp() throws FileNotFoundException {
    var modelLoaderRegistry = new ModelLoaderRegistry()
        .register(new YamlModelLoader());
    var mappingParser = new YamlModelMappingParser(new ComponentRegistry(), modelLoaderRegistry,
        new ValueTypeRegistry());
    var mapping = mappingParser.parse(new FileInputStream("../data/geo/mapping.yaml"));

    bldRepository = new FileSource(mapping.getSourceModel("bld"), Paths.get("../data/bld")).getDataRepository();

    var orchestration = Orchestration.builder()
        .modelMapping(mapping)
        .source("bld", () -> bldRepositoryStub)
        .build();

    graphQL = GraphQL.newGraphQL(SchemaFactory.create(orchestration))
        .build();
  }

  @Test
  void queryObject_withoutBatchLoading() {
    when(bldRepositoryStub.findOne(any(ObjectRequest.class)))
        .thenAnswer(invocation -> bldRepository.findOne(invocation.getArgument(0)));

    var result = graphQL.execute("""
          query {
            building(id: "B0003") {
              id
              surface
              hasAddress {
                id
                houseNumber
                postalCode
              }
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

    verify(bldRepositoryStub, times(4)).findOne(any(ObjectRequest.class));
    assertResult(result);
  }

  @Test
  void queryObject_withBatchLoading() {
    when(bldRepositoryStub.supportsBatchLoading(any())).thenReturn(true);

    when(bldRepositoryStub.findOne(any(ObjectRequest.class)))
        .thenAnswer(invocation -> bldRepository.findOne(invocation.getArgument(0)));

    when(bldRepositoryStub.findBatch(any(BatchRequest.class)))
        .thenAnswer(invocation -> bldRepository.findBatch(invocation.getArgument(0)));

    var result = graphQL.execute("""
          query {
            building(id: "B0003") {
              id
              surface
              hasAddress {
                id
                houseNumber
                postalCode
              }
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

    verify(bldRepositoryStub, times(2)).findOne(any(ObjectRequest.class));
    verify(bldRepositoryStub, times(1)).findBatch(any(BatchRequest.class));
    assertResult(result);
  }

  private void assertResult(ExecutionResult result) {
    Map<String, Object> data = result.getData();

    assertThat(result.getErrors()).isEmpty();
    assertThat(data).isNotNull()
        .hasEntrySatisfying("building", building ->
            assertThat(building).isNotNull()
                .isInstanceOf(Map.class)
                .asInstanceOf(map(String.class, Object.class))
                .containsEntry("id", "B0003")
                .containsEntry("surface", 254)
                .hasEntrySatisfying("hasAddress", hasAddress ->
                    assertThat(hasAddress).isNotNull()
                        .isInstanceOf(List.class)
                        .asInstanceOf(list(Map.class))
                        .hasSize(3))
                .hasEntrySatisfying("hasLineage", hasLineage ->
                    assertThat(hasLineage).isNotNull()
                        .isInstanceOf(Map.class)
                        .asInstanceOf(map(String.class, Object.class))
                        .hasEntrySatisfying("orchestratedProperties", orchestratedProperties ->
                            assertThat(orchestratedProperties).isNotNull()
                                .isInstanceOf(List.class)
                                .asInstanceOf(list(Map.class))
                                .hasSize(3))));
  }
}
