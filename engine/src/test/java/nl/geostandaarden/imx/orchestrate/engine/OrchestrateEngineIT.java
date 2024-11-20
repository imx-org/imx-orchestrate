package nl.geostandaarden.imx.orchestrate.engine;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.LIST;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import nl.geostandaarden.imx.orchestrate.engine.exchange.CollectionRequest;
import nl.geostandaarden.imx.orchestrate.engine.exchange.ObjectRequest;
import nl.geostandaarden.imx.orchestrate.engine.exchange.ObjectResult;
import nl.geostandaarden.imx.orchestrate.engine.source.DataRepository;
import nl.geostandaarden.imx.orchestrate.ext.spatial.SpatialExtension;
import nl.geostandaarden.imx.orchestrate.model.ComponentRegistry;
import nl.geostandaarden.imx.orchestrate.model.ModelMapping;
import nl.geostandaarden.imx.orchestrate.model.lineage.ObjectLineage;
import nl.geostandaarden.imx.orchestrate.model.loader.ModelLoaderRegistry;
import nl.geostandaarden.imx.orchestrate.model.types.ValueTypeFactory;
import nl.geostandaarden.imx.orchestrate.model.types.ValueTypeRegistry;
import nl.geostandaarden.imx.orchestrate.parser.yaml.YamlModelLoader;
import nl.geostandaarden.imx.orchestrate.parser.yaml.YamlModelMappingParser;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Point;
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

    @Mock
    private DataRepository landRepositoryMock;

    private OrchestrateEngine engine;

    @BeforeAll
    static void beforeAll() throws IOException {
        var modelLoaderRegistry = new ModelLoaderRegistry().register(new YamlModelLoader());
        var valueTypeRegistry = new ValueTypeRegistry();
        valueTypeRegistry.register(SPATIAL_EXTENSION.getValueTypeFactories().toArray(ValueTypeFactory[]::new));
        var mappingParser = new YamlModelMappingParser(new ComponentRegistry(), modelLoaderRegistry, valueTypeRegistry);
        MODEL_MAPPING = mappingParser.parse(new FileInputStream("../data/geo/mapping.yaml"));
    }

    @BeforeEach
    void beforeEach() {
        engine = OrchestrateEngine.builder()
                .modelMapping(MODEL_MAPPING)
                .source("adr", () -> adrRepositoryMock)
                .source("city", () -> cityRepositoryMock)
                .source("land", () -> landRepositoryMock)
                .extension(SPATIAL_EXTENSION)
                .build();
    }

    @Test
    void fetch_returnsBuilding_forBuildingMatch() {
        var targetModel = engine.getModelMapping().getTargetModel();

        var request = ObjectRequest.builder(targetModel)
                .objectType("Construction")
                .objectKey(Map.of("id", "BU0001"))
                .selectProperty("id")
                .selectObjectProperty("dimensions", builder -> builder.selectProperty("surface")
                        .build())
                .selectProperty("geometry")
                .selectCollectionProperty("hasAddress", builder -> builder.selectProperty("postalCode")
                        .selectProperty("houseNumber")
                        .selectObjectProperty("parcel", relBuilder -> relBuilder
                                .selectProperty("geometry")
                                .build())
                        .build())
                .build();

        when(adrRepositoryMock.findOne(any(ObjectRequest.class))).thenAnswer(invocation -> {
            var objectType = ((ObjectRequest) invocation.getArgument(0)).getObjectType();

            return switch (objectType.getName()) {
                case "Address" -> Mono.just(
                        Map.of("id", "A0001", "houseNumber", 23, "postalCode", "1234AB", "parcel", "P12345"));
                default -> throw new IllegalStateException();
            };
        });

        when(cityRepositoryMock.findOne(any(ObjectRequest.class))).thenAnswer(invocation -> {
            var objectType = ((ObjectRequest) invocation.getArgument(0)).getObjectType();

            return switch (objectType.getName()) {
                case "Building" -> Mono.just(Map.of(
                        "id",
                        "BU0001",
                        "area",
                        123,
                        "geometry",
                        Map.of(
                                "type",
                                "Polygon",
                                "coordinates",
                                List.of(List.of(
                                        List.of(0, 0),
                                        List.of(10, 0),
                                        List.of(10, 10),
                                        List.of(0, 10),
                                        List.of(0, 0))))));
                case "Bridge" -> Mono.empty();
                default -> throw new IllegalStateException();
            };
        });

        when(cityRepositoryMock.find(any(CollectionRequest.class))).thenAnswer(invocation -> {
            var objectType = ((CollectionRequest) invocation.getArgument(0)).getObjectType();

            return switch (objectType.getName()) {
                case "BuildingPart" -> Flux.just(Map.of("id", "BP0001", "hasMainAddress", Map.of("id", "A0001")));
                default -> throw new IllegalStateException();
            };
        });

        when(landRepositoryMock.findOne(any(ObjectRequest.class))).thenAnswer(invocation -> {
            var objectType = ((ObjectRequest) invocation.getArgument(0)).getObjectType();

            return switch (objectType.getName()) {
                case "Parcel" -> Mono.just(Map.of(
                        "id",
                        "P12345",
                        "geometry",
                        Map.of(
                                "type",
                                "Polygon",
                                "coordinates",
                                List.of(List.of(
                                        List.of(0, 0),
                                        List.of(10, 0),
                                        List.of(10, 10),
                                        List.of(0, 10),
                                        List.of(0, 0))))));
                default -> throw new IllegalStateException();
            };
        });

        var resultMono = engine.fetch(request);

        StepVerifier.create(resultMono)
                .assertNext(result -> {
                    assertThat(result).isNotNull();
                    assertThat(result.getProperties())
                            .containsEntry("id", "BU0001")
                            .containsEntry("dimensions", Map.of("surface", 123));
                    assertThat(result.getLineage())
                            .extracting(
                                    ObjectLineage::getOrchestratedDataElements,
                                    as(InstanceOfAssertFactories.COLLECTION))
                            .hasSize(3);
                })
                .verifyComplete();
    }

    @Test
    void fetch_returnsBridge_forBridgeMatch() {
        var targetModel = engine.getModelMapping().getTargetModel();

        var request = ObjectRequest.builder(targetModel)
                .objectType("Construction")
                .objectKey(Map.of("id", "BR0001"))
                .selectProperty("id")
                .selectObjectProperty("dimensions", builder -> builder.selectProperty("surface")
                        .build())
                .selectProperty("geometry")
                .selectCollectionProperty("hasAddress", builder -> builder.selectProperty("postalCode")
                        .selectProperty("houseNumber")
                        .build())
                .build();

        when(cityRepositoryMock.findOne(any(ObjectRequest.class))).thenAnswer(invocation -> {
            var objectType = ((ObjectRequest) invocation.getArgument(0)).getObjectType();

            return switch (objectType.getName()) {
                case "Building" -> Mono.empty();
                case "Bridge" -> Mono.just(
                        Map.of("id", "BR0001", "geometry", Map.of("type", "Point", "coordinates", List.of(0, 0))));
                default -> throw new IllegalStateException();
            };
        });

        var resultMono = engine.fetch(request);

        StepVerifier.create(resultMono)
                .assertNext(result -> {
                    assertThat(result).isNotNull();
                    assertThat(result.getProperties())
                            .containsEntry("id", "BR0001")
                            .containsKey("geometry")
                            .containsEntry("hasAddress", emptyList())
                            .doesNotContainKey("surface");
                    assertThat(result.getLineage())
                            .extracting(
                                    ObjectLineage::getOrchestratedDataElements,
                                    as(InstanceOfAssertFactories.COLLECTION))
                            .hasSize(2);
                })
                .verifyComplete();
    }

    @Test
    void fetch_returnsEmpty_forNoSourceMatch() {
        var targetModel = engine.getModelMapping().getTargetModel();

        var request = ObjectRequest.builder(targetModel)
                .objectType("Construction")
                .objectKey(Map.of("id", "BU0001"))
                .selectProperty("id")
                .selectObjectProperty("dimensions", builder -> builder.selectProperty("surface")
                        .build())
                .selectProperty("geometry")
                .build();

        when(cityRepositoryMock.findOne(any(ObjectRequest.class))).thenReturn(Mono.empty());

        var resultMono = engine.fetch(request);

        StepVerifier.create(resultMono).verifyComplete();
    }

    @Test
    void fetch_picksFirstResult_forMultipleMatches() {
        var targetModel = engine.getModelMapping().getTargetModel();

        var request = ObjectRequest.builder(targetModel)
                .objectType("Construction")
                .objectKey(Map.of("id", "BU0001"))
                .selectProperty("id")
                .selectProperty("geometry")
                .build();

        when(cityRepositoryMock.findOne(any(ObjectRequest.class))).thenAnswer(invocation -> {
            var objectType = ((ObjectRequest) invocation.getArgument(0)).getObjectType();

            return switch (objectType.getName()) {
                case "Building" -> Mono.just(Map.of(
                                "id", "BU0001", "geometry", Map.of("type", "Point", "coordinates", List.of(0, 0))))
                        .delayElement(Duration.ofMillis(100));
                case "Bridge" -> Mono.just(
                        Map.of("id", "BR0001", "geometry", Map.of("type", "Point", "coordinates", List.of(0, 0))));
                default -> throw new IllegalStateException();
            };
        });

        var resultMono = engine.fetch(request);

        StepVerifier.create(resultMono)
                .assertNext(result -> {
                    assertThat(result).isNotNull();
                    assertThat(result.getProperties()).containsEntry("id", "BU0001");
                })
                .verifyComplete();
    }

    @Test
    void fetch_returnsEmptyList_forEmptySources() {
        var targetModel = engine.getModelMapping().getTargetModel();

        var request = CollectionRequest.builder(targetModel)
                .objectType("Construction")
                .selectProperty("id")
                .selectProperty("geometry")
                .build();

        when(cityRepositoryMock.find(any(CollectionRequest.class))).thenReturn(Flux.empty());

        var resultMono = engine.fetch(request);

        StepVerifier.create(resultMono)
                .assertNext(result -> assertThat(result.getObjectResults()).isEmpty())
                .verifyComplete();
    }

    @Test
    void fetch_returnsMergedList_forMultipleSources() {
        var targetModel = engine.getModelMapping().getTargetModel();

        var request = CollectionRequest.builder(targetModel)
                .objectType("Construction")
                .selectProperty("id")
                .selectProperty("geometry")
                .build();

        when(cityRepositoryMock.find(any(CollectionRequest.class))).thenAnswer(invocation -> {
            var objectType = ((CollectionRequest) invocation.getArgument(0)).getObjectType();

            return switch (objectType.getName()) {
                case "Building" -> Flux.just(
                        Map.of("id", "BU0001", "geometry", Map.of("type", "Point", "coordinates", List.of(0, 0))));
                case "Bridge" -> Flux.just(
                        Map.of("id", "BR0001", "geometry", Map.of("type", "Point", "coordinates", List.of(0, 0))));
                default -> throw new IllegalStateException();
            };
        });

        var resultMono = engine.fetch(request);

        StepVerifier.create(resultMono)
                .assertNext(result -> assertThat(result.getObjectResults()).hasSize(2))
                .verifyComplete();
    }

    @Test
    void fetch_returnsAddressWithNestedBuilding_forDeepRelation() {
        var targetModel = engine.getModelMapping().getTargetModel();

        var request = ObjectRequest.builder(targetModel)
                .objectType("Address")
                .objectKey(Map.of("id", "A0001"))
                .selectProperty("id")
                .selectCollectionProperty("isAddressOf", builder -> builder.selectProperty("id")
                        .selectProperty("geometry")
                        .build())
                .build();

        when(adrRepositoryMock.findOne(any(ObjectRequest.class))).thenAnswer(invocation -> {
            var objectType = ((ObjectRequest) invocation.getArgument(0)).getObjectType();

            return switch (objectType.getName()) {
                case "Address" -> Mono.just(Map.of("id", "A0001"));
                default -> throw new IllegalStateException();
            };
        });

        when(cityRepositoryMock.findOne(any(ObjectRequest.class))).thenAnswer(invocation -> {
            var objectType = ((ObjectRequest) invocation.getArgument(0)).getObjectType();

            return switch (objectType.getName()) {
                case "Building" -> Mono.just(
                        Map.of("id", "BU0001", "geometry", Map.of("type", "Point", "coordinates", List.of(0, 0))));
                default -> throw new IllegalStateException();
            };
        });

        when(cityRepositoryMock.find(any(CollectionRequest.class))).thenAnswer(invocation -> {
            var collectionRequest = ((CollectionRequest) invocation.getArgument(0));
            var objectType = collectionRequest.getObjectType();
            var filter = collectionRequest.getFilter();

            if (!"BuildingPart".equals(objectType.getName()) || filter == null) {
                throw new IllegalStateException();
            }

            if ("hasSubAddress".equals(filter.getPath().getFirstSegment())) {
                return Flux.empty();
            }

            if ("hasMainAddress".equals(filter.getPath().getFirstSegment())) {
                return Flux.just(Map.of("id", "BP0001", "isPartOf", List.of(Map.of("id", "BU0001"))));
            }

            throw new IllegalStateException();
        });

        var resultMono = engine.fetch(request);

        StepVerifier.create(resultMono)
                .assertNext(result -> {
                    assertThat(result).isNotNull();
                    assertThat(result.getProperties())
                            .containsEntry("id", "A0001")
                            .extractingByKey("isAddressOf", LIST)
                            .singleElement()
                            .isInstanceOfSatisfying(ObjectResult.class, nestedResult -> {
                                assertThat(nestedResult).isNotNull();
                                assertThat(nestedResult.getProperties())
                                        .containsEntry("id", "BU0001")
                                        .hasEntrySatisfying("geometry", geometry -> assertThat(geometry)
                                                .isInstanceOf(Point.class));
                            });
                })
                .verifyComplete();
    }

    @Test
    void fetch_returnsAddressWithNestedBuilding_forDeepRelationWithOnlyKeySelected() {
        var targetModel = engine.getModelMapping().getTargetModel();

        var request = ObjectRequest.builder(targetModel)
                .objectType("Address")
                .objectKey(Map.of("id", "A0001"))
                .selectProperty("id")
                .selectCollectionProperty(
                        "isAddressOf", builder -> builder.selectProperty("id").build())
                .build();

        when(adrRepositoryMock.findOne(any(ObjectRequest.class))).thenAnswer(invocation -> {
            var objectType = ((ObjectRequest) invocation.getArgument(0)).getObjectType();

            return switch (objectType.getName()) {
                case "Address" -> Mono.just(Map.of("id", "A0001"));
                default -> throw new IllegalStateException();
            };
        });

        when(cityRepositoryMock.findOne(any(ObjectRequest.class))).thenAnswer(invocation -> {
            var objectType = ((ObjectRequest) invocation.getArgument(0)).getObjectType();

            return switch (objectType.getName()) {
                case "Building" -> Mono.just(Map.of("id", "BU0001"));
                default -> throw new IllegalStateException();
            };
        });

        when(cityRepositoryMock.find(any(CollectionRequest.class))).thenAnswer(invocation -> {
            var collectionRequest = ((CollectionRequest) invocation.getArgument(0));
            var objectType = collectionRequest.getObjectType();
            var filter = collectionRequest.getFilter();

            if (!"BuildingPart".equals(objectType.getName()) || filter == null) {
                throw new IllegalStateException();
            }

            if ("hasSubAddress".equals(filter.getPath().getFirstSegment())) {
                return Flux.empty();
            }

            if ("hasMainAddress".equals(filter.getPath().getFirstSegment())) {
                return Flux.just(Map.of("id", "BP0001", "isPartOf", List.of(Map.of("id", "BU0001"))));
            }

            throw new IllegalStateException();
        });

        var resultMono = engine.fetch(request);

        StepVerifier.create(resultMono)
                .assertNext(result -> {
                    assertThat(result).isNotNull();
                    assertThat(result.getProperties())
                            .containsEntry("id", "A0001")
                            .extractingByKey("isAddressOf", LIST)
                            .singleElement()
                            .isInstanceOfSatisfying(ObjectResult.class, nestedResult -> {
                                assertThat(nestedResult).isNotNull();
                                assertThat(nestedResult.getProperties()).containsEntry("id", "BU0001");
                            });
                })
                .verifyComplete();
    }
}
