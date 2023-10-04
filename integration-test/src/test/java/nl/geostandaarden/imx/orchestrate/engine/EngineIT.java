package nl.geostandaarden.imx.orchestrate.engine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.list;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import nl.geostandaarden.imx.orchestrate.engine.exchange.BatchRequest;
import nl.geostandaarden.imx.orchestrate.engine.exchange.CollectionRequest;
import nl.geostandaarden.imx.orchestrate.engine.exchange.ObjectRequest;
import nl.geostandaarden.imx.orchestrate.engine.exchange.ObjectResult;
import nl.geostandaarden.imx.orchestrate.engine.source.DataRepository;
import nl.geostandaarden.imx.orchestrate.model.ComponentRegistry;
import nl.geostandaarden.imx.orchestrate.model.ModelMapping;
import nl.geostandaarden.imx.orchestrate.model.loader.ModelLoaderRegistry;
import nl.geostandaarden.imx.orchestrate.model.types.ValueTypeRegistry;
import nl.geostandaarden.imx.orchestrate.parser.yaml.YamlModelLoader;
import nl.geostandaarden.imx.orchestrate.parser.yaml.YamlModelMappingParser;
import nl.geostandaarden.imx.orchestrate.source.file.FileSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class EngineIT {

  private DataRepository bldRepository;

  @Mock
  private DataRepository bldRepositoryStub;

  private ModelMapping modelMapping;

  private OrchestrateEngine engine;

  @BeforeEach
  void setUp() throws FileNotFoundException {
    var modelLoaderRegistry = new ModelLoaderRegistry()
        .register(new YamlModelLoader());
    var mappingParser = new YamlModelMappingParser(new ComponentRegistry(), modelLoaderRegistry,
        new ValueTypeRegistry());

    modelMapping = mappingParser.parse(new FileInputStream("../data/geo/mapping.yaml"));
    bldRepository = new FileSource(modelMapping.getSourceModel("bld"), Paths.get("../data/bld")).getDataRepository();

    engine = OrchestrateEngine.builder()
        .modelMapping(modelMapping)
        .source("bld", () -> bldRepositoryStub)
        .build();
  }

  @Test
  void queryObject_withoutBatchLoading() {
    when(bldRepositoryStub.findOne(any(ObjectRequest.class)))
        .thenAnswer(invocation -> bldRepository.findOne(invocation.getArgument(0)));

    when(bldRepositoryStub.find(any(CollectionRequest.class)))
        .thenAnswer(invocation -> bldRepository.find(invocation.getArgument(0)));

    var request = ObjectRequest.builder(modelMapping.getTargetModel())
        .objectType("Construction")
        .objectKey(Map.of("id", "B0002"))
        .selectProperty("id")
        .selectProperty("surface")
        .selectCollectionProperty("hasAddress", b1 -> b1
            .selectProperty("id")
            .selectProperty("houseNumber")
            .selectProperty("postalCode")
            .build())
        .build();

    var result = engine.fetch(request);

    StepVerifier.create(result)
        .assertNext(this::assertResult)
        .verifyComplete();

    verify(bldRepositoryStub, times(5)).findOne(any(ObjectRequest.class));
    verify(bldRepositoryStub, times(1)).find(any(CollectionRequest.class));
  }

  @Test
  void queryObject_withBatchLoading() {
    when(bldRepositoryStub.supportsBatchLoading(any())).thenReturn(true);

    when(bldRepositoryStub.findOne(any(ObjectRequest.class)))
        .thenAnswer(invocation -> bldRepository.findOne(invocation.getArgument(0)));

    when(bldRepositoryStub.find(any(CollectionRequest.class)))
        .thenAnswer(invocation -> bldRepository.find(invocation.getArgument(0)));

    when(bldRepositoryStub.findBatch(any(BatchRequest.class)))
        .thenAnswer(invocation -> bldRepository.findBatch(invocation.getArgument(0)));

    var request = ObjectRequest.builder(modelMapping.getTargetModel())
        .objectType("Construction")
        .objectKey(Map.of("id", "B0002"))
        .selectProperty("id")
        .selectProperty("surface")
        .selectCollectionProperty("hasAddress", b1 -> b1
            .selectProperty("id")
            .selectProperty("houseNumber")
            .selectProperty("postalCode")
            .build())
        .build();

    var result = engine.fetch(request);

    StepVerifier.create(result)
        .assertNext(this::assertResult)
        .verifyComplete();

    verify(bldRepositoryStub, times(1)).findOne(any(ObjectRequest.class));
    verify(bldRepositoryStub, times(1)).find(any(CollectionRequest.class));
    verify(bldRepositoryStub, times(2)).findBatch(any(BatchRequest.class));
  }

  private void assertResult(ObjectResult result) {
    assertThat(result.getProperties()).isNotNull()
        .containsEntry("id", "B0002")
        .containsEntry("surface", 195)
        .hasEntrySatisfying("hasAddress", hasAddress ->
            assertThat(hasAddress).isNotNull()
                .isInstanceOf(List.class)
                .asInstanceOf(list(Map.class))
                .hasSize(4));
  }
}
