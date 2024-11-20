package nl.geostandaarden.imx.orchestrate.source.file;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Map;
import nl.geostandaarden.imx.orchestrate.engine.exchange.CollectionRequest;
import nl.geostandaarden.imx.orchestrate.engine.exchange.ObjectRequest;
import nl.geostandaarden.imx.orchestrate.model.Attribute;
import nl.geostandaarden.imx.orchestrate.model.Model;
import nl.geostandaarden.imx.orchestrate.model.ObjectType;
import nl.geostandaarden.imx.orchestrate.model.types.ScalarTypes;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

class FileRepositoryTest {

    private static final Model MODEL = createModel();

    private static final ObjectNode BUILDING1_NODE =
            new ObjectMapper().createObjectNode().put("id", 1).put("name", "Building 1");

    private static final ObjectNode BUILDING2_NODE =
            new ObjectMapper().createObjectNode().put("id", 2).put("name", "Building 2");

    private static final Map<String, Object> BUILDING1_MAP = Map.of("id", 1, "name", "Building 1");

    private static final Map<String, Object> BUILDING2_MAP = Map.of("id", 2, "name", "Building 2");

    private final FileRepository fileRepository = new FileRepository();

    public FileRepositoryTest() {
        fileRepository.add("Building", Map.of("id", 1), BUILDING1_NODE);
        fileRepository.add("Building", Map.of("id", 2), BUILDING2_NODE);
    }

    @Test
    void findOne_ReturnsObject_IfExists() {
        var objectRequest = ObjectRequest.builder(MODEL)
                .objectType("Building")
                .objectKey(Map.of("id", 1))
                .selectProperty("id")
                .selectProperty("name")
                .build();
        var result = fileRepository.findOne(objectRequest);

        StepVerifier.create(result).expectNext(BUILDING1_MAP).verifyComplete();
    }

    @Test
    void findOne_ReturnsEmptyMono_IfNotExists() {
        var objectRequest = ObjectRequest.builder(MODEL)
                .objectType("Building")
                .objectKey(Map.of("id", 123))
                .selectProperty("id")
                .selectProperty("name")
                .build();
        var result = fileRepository.findOne(objectRequest);

        StepVerifier.create(result).verifyComplete();
    }

    @Test
    void find_ReturnsObjects_ForAllObjects() {
        var collectionRequest = CollectionRequest.builder(MODEL)
                .objectType("Building")
                .selectProperty("id")
                .selectProperty("name")
                .build();

        var result = fileRepository.find(collectionRequest);

        StepVerifier.create(result)
                .expectNext(BUILDING1_MAP)
                .expectNext(BUILDING2_MAP)
                .verifyComplete();
    }

    private static ObjectType createBuildingType() {
        return ObjectType.builder()
                .name("Building")
                .property(Attribute.builder()
                        .identifier(true)
                        .name("id")
                        .type(ScalarTypes.INTEGER)
                        .build())
                .property(Attribute.builder()
                        .name("name")
                        .type(ScalarTypes.STRING)
                        .build())
                .build();
    }

    private static Model createModel() {
        return Model.builder().objectType(createBuildingType()).build();
    }
}
