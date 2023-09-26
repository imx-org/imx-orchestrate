package nl.geostandaarden.imx.orchestrate.engine;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import nl.geostandaarden.imx.orchestrate.engine.exchange.ObjectRequest;
import nl.geostandaarden.imx.orchestrate.engine.source.DataRepository;
import nl.geostandaarden.imx.orchestrate.model.ComponentRegistry;
import nl.geostandaarden.imx.orchestrate.model.ModelMapping;
import nl.geostandaarden.imx.orchestrate.model.loader.ModelLoaderRegistry;
import nl.geostandaarden.imx.orchestrate.model.types.ValueTypeRegistry;
import nl.geostandaarden.imx.orchestrate.parser.yaml.YamlModelLoader;
import nl.geostandaarden.imx.orchestrate.parser.yaml.YamlModelMappingParser;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class OrchestrateEngineTest {

  private static ModelMapping MODEL_MAPPING;

  @Mock
  private DataRepository dataRepositoryMock;

  private OrchestrateEngine engine;

  @BeforeAll
  static void beforeAll() throws IOException {
    var modelLoaderRegistry = new ModelLoaderRegistry()
        .register(new YamlModelLoader());
    var mappingParser = new YamlModelMappingParser(new ComponentRegistry(), modelLoaderRegistry,
        new ValueTypeRegistry());
    MODEL_MAPPING = mappingParser.parse(new FileInputStream("../data/geo/mapping.yaml"));
  }

  @BeforeEach
  void beforeEach() {
    engine = OrchestrateEngine.builder()
        .modelMapping(MODEL_MAPPING)
        .source("bld", () -> dataRepositoryMock)
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
        .selectProperty("surface")
        .selectCollectionProperty("hasAddress", builder -> builder
            .selectProperty("postalCode")
            .selectProperty("houseNumber")
            .build())
        .build();

    when(dataRepositoryMock.findOne(any(ObjectRequest.class)))
        .thenReturn(Mono.just(Map.of("id", "B0001", "area", 123)));

    var resultMono = engine.fetch(request);

    StepVerifier.create(resultMono)
        .assertNext(result -> {
          System.out.println(result);
        })
        .expectComplete()
        .verify();
  }
}
