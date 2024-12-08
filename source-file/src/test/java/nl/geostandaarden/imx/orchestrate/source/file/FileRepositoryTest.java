package nl.geostandaarden.imx.orchestrate.source.file;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import nl.geostandaarden.imx.orchestrate.engine.selection.SelectionBuilder;
import nl.geostandaarden.imx.orchestrate.model.Attribute;
import nl.geostandaarden.imx.orchestrate.model.Model;
import nl.geostandaarden.imx.orchestrate.model.ObjectType;
import nl.geostandaarden.imx.orchestrate.model.types.ScalarTypes;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

class FileRepositoryTest {

    private static final Model MODEL = createModel();

    private static final com.fasterxml.jackson.databind.node.ObjectNode BUILDING1_NODE =
            new ObjectMapper().createObjectNode().put("id", 1).put("name", "Building 1");

    private static final com.fasterxml.jackson.databind.node.ObjectNode BUILDING2_NODE =
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
        var selection = SelectionBuilder.newObjectNode(MODEL, "Building")
                .objectKey(Map.of("id", 1))
                .select("id")
                .select("name")
                .build();

        var request = selection.toRequest();
        var result = fileRepository.findOne(request);

        StepVerifier.create(result).expectNext(BUILDING1_MAP).verifyComplete();
    }

    @Test
    void findOne_ReturnsEmptyMono_IfNotExists() {
        var selection = SelectionBuilder.newObjectNode(MODEL, "Building")
                .objectKey(Map.of("id", 123))
                .select("id")
                .select("name")
                .build();

        var request = selection.toRequest();
        var result = fileRepository.findOne(request);

        StepVerifier.create(result).verifyComplete();
    }

    @Test
    void find_ReturnsObjects_ForAllObjects() {
        var selection = SelectionBuilder.newCollectionNode(MODEL, "Building")
                .select("id")
                .select("name")
                .build();

        var request = selection.toRequest();
        var result = fileRepository.find(request);

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
        return Model.builder() //
                .objectType(createBuildingType())
                .build();
    }
}
