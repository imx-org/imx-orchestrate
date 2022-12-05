package org.dotwebstack.orchestrate.model.mapping;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class ObjectTypeRefTest {

  @Test
  void fromString_Succeeds_ForValidInput() {
    var ref = ObjectTypeRef.fromString("src:City");

    assertThat(ref.getSourceAlias()).isEqualTo("src");
    assertThat(ref.getName()).isEqualTo("City");
  }

  @Test
  void fromString_ThrowsException_ForInvalidInput() {
    assertThatThrownBy(() -> ObjectTypeRef.fromString("src:City/name"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Object type references must match pattern: ^(\\w+):(\\w+)$");
  }
}
