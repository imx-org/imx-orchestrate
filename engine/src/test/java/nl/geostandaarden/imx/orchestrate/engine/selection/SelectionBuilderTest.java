package nl.geostandaarden.imx.orchestrate.engine.selection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import nl.geostandaarden.imx.orchestrate.model.Attribute;
import nl.geostandaarden.imx.orchestrate.model.Model;
import nl.geostandaarden.imx.orchestrate.model.ModelException;
import nl.geostandaarden.imx.orchestrate.model.ObjectType;
import nl.geostandaarden.imx.orchestrate.model.ObjectTypeRef;
import nl.geostandaarden.imx.orchestrate.model.Relation;
import nl.geostandaarden.imx.orchestrate.model.types.ScalarTypes;
import org.junit.jupiter.api.Test;

class SelectionBuilderTest {

    private final Model model;

    SelectionBuilderTest() {
        model = Model.builder()
                .objectType(ObjectType.builder()
                        .name("Building")
                        .property(Attribute.builder()
                                .name("surface")
                                .type(ScalarTypes.INTEGER)
                                .build())
                        .property(Relation.builder()
                                .name("city")
                                .target(ObjectTypeRef.forType("City"))
                                .build())
                        .build())
                .objectType(ObjectType.builder()
                        .name("City")
                        .property(Attribute.builder()
                                .name("name")
                                .type(ScalarTypes.STRING)
                                .build())
                        .build())
                .build();
    }

    @Test
    void builder_Succeeds_ForValidProperties() {
        var buildingType = model.getObjectType("Building");
        var cityType = model.getObjectType("City");

        var selection = SelectionBuilder.newObjectNode(model, buildingType.getName())
                .select("surface")
                .selectObject("city", b -> b.select("name").build())
                .build();

        assertThat(selection.getChildNodes())
                .hasEntrySatisfying("surface", node -> assertAttribute(node, buildingType, "surface"))
                .hasEntrySatisfying("city", cityNode -> assertThat(cityNode)
                        .isInstanceOfSatisfying(ObjectNode.class, cityObjectNode -> {
                            assertThat(cityObjectNode.getRelation()).isEqualTo(buildingType.getRelation("city"));
                            assertThat(cityObjectNode.getObjectType()).isEqualTo(cityType);
                            assertThat(cityObjectNode.getChildNodes())
                                    .hasEntrySatisfying("name", node -> assertAttribute(node, cityType, "name"));
                        }));
    }

    @Test
    void builder_ThrowsException_ForUnknownObjectType() {
        assertThatThrownBy(() -> SelectionBuilder.newObjectNode(model, "Foo"))
                .isInstanceOf(ModelException.class)
                .hasMessage("Object type not found: Foo");
    }

    @Test
    void builder_ThrowsException_ForUnknownProperty() {
        var builder = SelectionBuilder.newObjectNode(model, "Building");

        assertThatThrownBy(() -> builder.select("foo"))
                .isInstanceOf(ModelException.class)
                .hasMessage("Attribute not found: foo");
    }

    @Test
    void builder_ThrowsException_ForAttributeAsObjectProperty() {
        var builder = SelectionBuilder.newObjectNode(model, "Building");

        assertThatThrownBy(() -> builder.selectObject("surface", SelectionBuilder.ObjectNodeBuilder::build))
                .isInstanceOf(ModelException.class)
                .hasMessage("Relation not found: surface");
    }

    private void assertAttribute(TreeNode node, ObjectType objectType, String name) {
        assertThat(node).isInstanceOfSatisfying(AttributeNode.class, n -> assertThat(n.getAttribute())
                .isEqualTo(objectType.getAttribute(name)));
    }
}
