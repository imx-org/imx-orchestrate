package nl.geostandaarden.imx.orchestrate.engine.exchange;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import nl.geostandaarden.imx.orchestrate.engine.OrchestrateException;
import nl.geostandaarden.imx.orchestrate.model.*;
import nl.geostandaarden.imx.orchestrate.model.types.ScalarTypes;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ObjectRequestTest {

  private static Model model;

  @BeforeAll
  static void beforeAll() {
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
    var buildingRequest = ObjectRequest.builder(model)
        .objectType("Building")
        .selectProperty("surface")
        .selectObjectProperty("city", b -> b.selectProperty("name")
            .build())
        .build();

    var buildingType = model.getObjectType("Building");

    assertThat(buildingRequest.getSelectedProperties()).satisfiesExactly(
        surfaceProperty -> {
          assertThat(surfaceProperty.getProperty()).isSameAs(buildingType.getProperty("surface"));
          assertThat(surfaceProperty.getNestedRequest()).isNull();
        },
        cityProperty -> {
          assertThat(cityProperty.getProperty()).isSameAs(buildingType.getProperty("city"));

          var cityRequest = cityProperty.getNestedRequest();
          var cityType = model.getObjectType("City");

          assertThat(cityRequest.getObjectType()).isSameAs(cityType);
          assertThat(cityRequest.getSelectedProperties()).satisfiesExactly(
              nameProperty -> {
                assertThat(nameProperty.getProperty()).isSameAs(cityType.getProperty("name"));
                assertThat(nameProperty.getNestedRequest()).isNull();
              }
          );
        });
  }

  @Test
  void builder_ThrowsException_ForUnknownObjectType() {
    var requestBuilder = ObjectRequest.builder(model);

    assertThatThrownBy(() -> requestBuilder.objectType("Foo"))
        .isInstanceOf(ModelException.class)
        .hasMessage("Object type not found: Foo");
  }

  @Test
  void builder_ThrowsException_ForUnknownProperty() {
    var requestBuilder = ObjectRequest.builder(model)
        .objectType("Building");

    assertThatThrownBy(() -> requestBuilder.selectProperty("foo"))
        .isInstanceOf(ModelException.class)
        .hasMessage("Attribute not found: foo");
  }

  @Test
  void builder_ThrowsException_ForAttributeAsObjectProperty() {
    var requestBuilder = ObjectRequest.builder(model)
        .objectType("Building");

    assertThatThrownBy(() -> requestBuilder.selectObjectProperty("surface", b -> b.selectProperty("name").build()))
        .isInstanceOf(OrchestrateException.class)
        .hasMessage("Child selection can only be applied on relation properties.");
  }
}
