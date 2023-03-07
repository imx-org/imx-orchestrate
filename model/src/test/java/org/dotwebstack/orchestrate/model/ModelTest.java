package org.dotwebstack.orchestrate.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.dotwebstack.orchestrate.model.types.ScalarTypes;
import org.junit.jupiter.api.Test;

class ModelTest {

  @Test
  void builder_ResolvesRefs_ForObjectTypeRefs() {
    var model = Model.builder()
        .objectType(ObjectType.builder()
            .name("Person")
            .property(Attribute.builder()
                .name("id")
                .type(ScalarTypes.INTEGER)
                .cardinality(Cardinality.REQUIRED)
                .identifier(true)
                .build())
            .property(Attribute.builder()
                .name("name")
                .type(ScalarTypes.STRING)
                .build())
            .build())
        .objectType(ObjectType.builder()
            .name("City")
            .property(Attribute.builder()
                .name("id")
                .type(ScalarTypes.INTEGER)
                .cardinality(Cardinality.REQUIRED)
                .identifier(true)
                .build())
            .property(Attribute.builder()
                .name("name")
                .type(ScalarTypes.STRING)
                .cardinality(Cardinality.REQUIRED)
                .build())
            .property(Relation.builder()
                .name("mayor")
                .target(ObjectTypeRef.forType("Person"))
                .build())
            .build())
        .build();

    var cityType = model.getObjectType("City");
    assertThat(cityType.getProperties()).hasSize(3);
  }
}
