package org.dotwebstack.orchestrate.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class SourceTypeRefTest {

  @Test
  void fromString_Succeeds_ForValidInput() {
    var ref = SourceTypeRef.fromString("src:City");

    assertThat(ref.getModelAlias()).isEqualTo("src");
    assertThat(ref.getObjectType()).isEqualTo("City");
  }

  @Test
  void fromString_ThrowsException_ForInvalidInput() {
    assertThatThrownBy(() -> SourceTypeRef.fromString("src:City/name"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Object type references must match pattern: ^(\\w+):(\\w+)$");
  }
}
