package org.dotwebstack.orchestrate.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;

class ModelMappingTest {

  @Test
  void builder_Succeeds_Always() {
    var targetModel = mock(Model.class);
    var sourceModel = mock(Model.class);

    var modelMapping = ModelMapping.builder()
        .targetModel(targetModel)
        .sourceModel("src", sourceModel)
        .objectTypeMapping("Area", ObjectTypeMapping.builder()
            .sourceRoot(ObjectTypeRef.fromString("src:City"))
            .propertyMapping("code", PropertyMapping.builder()
                .pathMapping(PropertyPathMapping.builder()
                  .path(PropertyPath.fromString("id"))
                  .build())
                .build())
            .propertyMapping("manager", PropertyMapping.builder()
                .pathMapping(PropertyPathMapping.builder()
                    .path(PropertyPath.fromString("mayor/name"))
                    .build())
                .build())
            .build())
        .build();

    assertThat(modelMapping).isNotNull();
  }
}
