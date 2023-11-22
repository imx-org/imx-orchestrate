package nl.geostandaarden.imx.orchestrate.engine;

import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import nl.geostandaarden.imx.orchestrate.engine.exchange.CollectionRequest;
import nl.geostandaarden.imx.orchestrate.engine.exchange.ObjectRequest;
import nl.geostandaarden.imx.orchestrate.engine.source.DataRepository;
import nl.geostandaarden.imx.orchestrate.ext.spatial.SpatialExtension;
import nl.geostandaarden.imx.orchestrate.model.ComponentRegistry;
import nl.geostandaarden.imx.orchestrate.model.ModelMapping;
import nl.geostandaarden.imx.orchestrate.model.lineage.ObjectLineage;
import nl.geostandaarden.imx.orchestrate.model.loader.ModelLoaderRegistry;
import nl.geostandaarden.imx.orchestrate.model.types.ValueTypeRegistry;
import nl.geostandaarden.imx.orchestrate.parser.yaml.YamlModelLoader;
import nl.geostandaarden.imx.orchestrate.parser.yaml.YamlModelMappingParser;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class OrchestrateEngineIT {

  private static final SpatialExtension SPATIAL_EXTENSION = new SpatialExtension();

  private static ModelMapping MODEL_MAPPING;

  @Mock
  private DataRepository adrRepositoryMock;

  @Mock
  private DataRepository cityRepositoryMock;

  private OrchestrateEngine engine;

  @BeforeAll
  static void beforeAll() throws IOException {
    var modelLoaderRegistry = new ModelLoaderRegistry()
        .register(new YamlModelLoader());
    var valueTypeRegistry = new ValueTypeRegistry();
    SPATIAL_EXTENSION.registerValueTypes(valueTypeRegistry);
    var mappingParser = new YamlModelMappingParser(new ComponentRegistry(), modelLoaderRegistry, valueTypeRegistry);
    MODEL_MAPPING = mappingParser.parse(new FileInputStream("../data/geo/mapping.yaml"));
  }

  @BeforeEach
  void beforeEach() {
    engine = OrchestrateEngine.builder()
        .modelMapping(MODEL_MAPPING)
        .source("adr", () -> adrRepositoryMock)
        .source("city", () -> cityRepositoryMock)
        .extension(SPATIAL_EXTENSION)
        .build();
  }

  @Test
  void fetch() {
    var targetModel = engine.getModelMapping()
        .getTargetModel();

    var request = ObjectRequest.builder(targetModel)
        .objectType("Construction")
        .objectKey(Map.of("id", "B0001"))
        .selectProperty("id")
        .selectObjectProperty("dimensions", builder -> builder
            .selectProperty("surface")
            .build())
        .selectProperty("geometry")
        .selectCollectionProperty("hasAddress", builder -> builder
            .selectProperty("postalCode")
            .selectProperty("houseNumber")
            .build())
        .build();

    when(adrRepositoryMock.findOne(any(ObjectRequest.class)))
        .thenAnswer(invocation -> {
          var objectType = ((ObjectRequest) invocation.getArgument(0)).getObjectType();

          return switch (objectType.getName()) {
            case "Address" -> Mono.just(Map.of("id", "A0001", "houseNumber", 23, "postalCode", "1234AB"));
            default -> throw new IllegalStateException();
          };
        });

    when(cityRepositoryMock.findOne(any(ObjectRequest.class)))
        .thenAnswer(invocation -> {
          var objectType = ((ObjectRequest) invocation.getArgument(0)).getObjectType();

          return switch (objectType.getName()) {
            case "Building" -> Mono.just(Map.of("id", "B0001", "area", 123, "geometry",
                Map.of("type", "Polygon", "coordinates",
                    List.of(List.of(List.of(0, 0), List.of(10, 0), List.of(10, 10), List.of(0, 10), List.of(0, 0))))));
            default -> throw new IllegalStateException();
          };
        });

    when(cityRepositoryMock.find(any(CollectionRequest.class)))
        .thenAnswer(invocation -> {
          var objectType = ((CollectionRequest) invocation.getArgument(0)).getObjectType();

          return switch (objectType.getName()) {
            case "BuildingPart" -> Flux.just(Map.of("id", "BP0001", "hasMainAddress", Map.of("id", "A0001")));
            default -> throw new IllegalStateException();
          };
        });

    var resultMono = engine.fetch(request);

    StepVerifier.create(resultMono)
        .assertNext(result -> {
          assertThat(result).isNotNull();
          assertThat(result.getProperties())
              .containsEntry("id", "B0001")
              .containsEntry("dimensions", Map.of("surface", 123));
          assertThat(result.getLineage())
              .extracting(ObjectLineage::getOrchestratedDataElements, as(InstanceOfAssertFactories.COLLECTION))
              .hasSize(4);
        })
        .verifyComplete();
  }
}
