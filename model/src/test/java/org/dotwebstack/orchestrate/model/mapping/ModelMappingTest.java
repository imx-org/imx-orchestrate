package org.dotwebstack.orchestrate.model.mapping;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ModelMappingTest {

  @Test
  void builder_Succeeds_Always() {
    var modelMapping = ModelMapping.builder()
        .objectTypeMapping("Area", ObjectTypeMapping.builder()
            .sourceRoot(ObjectTypeRef.fromString("src:City"))
            .fieldMapping("id", FieldMapping.builder()
                .sourcePath(FieldPath.fromString("id"))
                .build())
            .fieldMapping("manager", FieldMapping.builder()
                .sourcePath(FieldPath.fromString("mayor/name"))
                .build())
            .build())
        .build();

    assertThat(modelMapping).isNotNull();
  }
}
