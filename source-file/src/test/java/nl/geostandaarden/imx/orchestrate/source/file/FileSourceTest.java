package nl.geostandaarden.imx.orchestrate.source.file;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.file.Paths;
import nl.geostandaarden.imx.orchestrate.engine.exchange.CollectionRequest;
import nl.geostandaarden.imx.orchestrate.engine.selection.AttributeNode;
import nl.geostandaarden.imx.orchestrate.engine.selection.CollectionNode;
import nl.geostandaarden.imx.orchestrate.model.Attribute;
import nl.geostandaarden.imx.orchestrate.model.Model;
import nl.geostandaarden.imx.orchestrate.model.ObjectType;
import nl.geostandaarden.imx.orchestrate.model.ObjectTypeRef;
import nl.geostandaarden.imx.orchestrate.model.Relation;
import nl.geostandaarden.imx.orchestrate.model.types.ScalarTypes;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.test.StepVerifier;

class FileSourceTest {

    private static final Model MODEL = createModel();

    @Test
    void constructor_PopulatesRepository_ForValidFolder() {
        var folderPath = Paths.get("src/test/resources/source1");
        var fileSource = new FileSource(MODEL, folderPath);
        var objectType = MODEL.getObjectType("Building");

        var selection = CollectionNode.builder()
                .objectType(objectType)
                .childNode("id", AttributeNode.forAttribute(objectType.getAttribute("id")))
                .childNode("name", AttributeNode.forAttribute(objectType.getAttribute("name")))
                .build();

        var objectFlux = fileSource
                .getDataRepository()
                .find(CollectionRequest.builder() //
                        .selection(selection)
                        .build());

        StepVerifier.create(objectFlux).expectNextCount(3).verifyComplete();
    }

    @ParameterizedTest
    @ValueSource(strings = {"src/test/resources/source", "src/test/resources/source1/Address.json"})
    void constructor_ThrowsException_ForNonExistingOrFilePath(String path) {
        var folderPath = Paths.get(path);
        assertThatThrownBy(() -> new FileSource(MODEL, folderPath))
                .hasMessage("File path does not exist or is not a " + "directory.");
    }

    private static Model createModel() {
        return Model.builder()
                .objectType(ObjectType.builder()
                        .name("Building")
                        .property(Attribute.builder()
                                .identifier(true)
                                .name("id")
                                .type(ScalarTypes.STRING)
                                .build())
                        .property(Attribute.builder()
                                .name("name")
                                .type(ScalarTypes.STRING)
                                .build())
                        .property(Relation.builder()
                                .name("address")
                                .target(ObjectTypeRef.forType("Address"))
                                .build())
                        .build())
                .objectType(ObjectType.builder()
                        .name("Address")
                        .property(Attribute.builder()
                                .identifier(true)
                                .name("id")
                                .type(ScalarTypes.STRING)
                                .build())
                        .property(Attribute.builder()
                                .name("name")
                                .type(ScalarTypes.STRING)
                                .build())
                        .build())
                .build();
    }
}
