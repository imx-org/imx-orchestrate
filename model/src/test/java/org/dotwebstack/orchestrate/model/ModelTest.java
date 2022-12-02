package org.dotwebstack.orchestrate.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.dotwebstack.orchestrate.model.type.ObjectField;
import org.dotwebstack.orchestrate.model.type.ObjectField.Cardinality;
import org.dotwebstack.orchestrate.model.type.ObjectType;
import org.dotwebstack.orchestrate.model.type.ScalarTypes;
import org.junit.jupiter.api.Test;

class ModelTest {

  @Test
  void getObjectType() {
    var model = Model.builder()
        .objectType(ObjectType.builder()
            .name("City")
            .field(ObjectField.builder()
                .name("name")
                .type(ScalarTypes.STRING)
                .cardinality(Cardinality.REQUIRED)
                .build())
            .field(ObjectField.builder()
                .name("nameAliases")
                .type(ScalarTypes.STRING)
                .cardinality(Cardinality.MULTI)
                .build())
            .field(ObjectField.builder()
                .name("population")
                .type(ScalarTypes.INTEGER)
                .build())
            .build())
        .build();

    assertThat(model).isNotNull();
  }
}
