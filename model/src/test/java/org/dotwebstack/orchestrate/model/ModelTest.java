package org.dotwebstack.orchestrate.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.dotwebstack.orchestrate.model.types.Field;
import org.dotwebstack.orchestrate.model.types.Field.Cardinality;
import org.dotwebstack.orchestrate.model.types.ObjectType;
import org.dotwebstack.orchestrate.model.types.ScalarTypes;
import org.dotwebstack.orchestrate.model.types.TypeRef;
import org.junit.jupiter.api.Test;

class ModelTest {

  @Test
  void builder_ResolvesRefs_ForObjectTypeRefs() {
    var model = Model.builder()
        .objectType(ObjectType.builder()
            .name("Person")
            .field(Field.builder()
                .name("id")
                .type(ScalarTypes.INTEGER)
                .cardinality(Cardinality.REQUIRED)
                .identifier(true)
                .build())
            .field(Field.builder()
                .name("name")
                .type(ScalarTypes.STRING)
                .build())
            .build())
        .objectType(ObjectType.builder()
            .name("City")
            .field(Field.builder()
                .name("id")
                .type(ScalarTypes.INTEGER)
                .cardinality(Cardinality.REQUIRED)
                .identifier(true)
                .build())
            .field(Field.builder()
                .name("name")
                .type(ScalarTypes.STRING)
                .cardinality(Cardinality.REQUIRED)
                .build())
            .field(Field.builder()
                .name("mayor")
                .type(TypeRef.forType("Person"))
                .build())
            .build())
        .build();

    var cityType = model.getObjectType("City")
        .orElseThrow();
    var mayorField = cityType.getField("mayor")
        .orElseThrow();

    assertThat(cityType.getFields()).hasSize(3);
    assertThat(mayorField.getType()).isInstanceOf(ObjectType.class);
  }
}
