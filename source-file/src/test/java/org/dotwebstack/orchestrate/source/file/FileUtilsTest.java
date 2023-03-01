package org.dotwebstack.orchestrate.source.file;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.dotwebstack.orchestrate.source.file.FileUtils.getBaseName;
import static org.dotwebstack.orchestrate.source.file.FileUtils.getObjectKey;
import static org.dotwebstack.orchestrate.source.file.FileUtils.getObjectProperties;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;
import org.dotwebstack.orchestrate.model.Attribute;
import org.dotwebstack.orchestrate.model.ObjectType;
import org.dotwebstack.orchestrate.model.types.ScalarTypes;
import org.dotwebstack.orchestrate.source.SelectedProperty;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class FileUtilsTest {

  @ParameterizedTest
  @MethodSource("pathsWithExtension")
  void getBaseName_ReturnsNameWithoutExtension_ForPathWithExtension(String input, String expected) {
    var baseName = getBaseName(Paths.get(input));
    assertThat(baseName).isEqualTo(expected);
  }

  @ParameterizedTest
  @ValueSource(strings = {"Foo", "foo/bar/Foo", "/foo/bar/Foo"})
  void getBaseName_ThrowsException_ForPathWithoutException(String input) {
    var path = Paths.get(input);
    assertThatThrownBy(() -> getBaseName(path)).isInstanceOf(IllegalArgumentException.class)
        .hasMessage("File path does not have an extension.");
  }

  @Test
  void getObjectKey_ReturnsKeyMap_ForObjectType() {
    var objectKey = getObjectKey(createObjectNode(), createObjectType());

    assertThat(objectKey).containsEntry("id", 1)
        .doesNotContainKey("name");
  }

  @Test
  void getObjectProperties_ReturnsProperties_ForSelectedProperties() {
    var objectNode = createObjectNode();
    var objectType = createObjectType();
    var selectedProperties = List.of(
        new SelectedProperty(objectType.getProperty("id")),
        new SelectedProperty(objectType.getProperty("name")));

    var objectProperties = getObjectProperties(objectNode, selectedProperties);

    assertThat(objectProperties).containsEntry("id", 1)
        .containsEntry("name", "Building 1");
  }

  private static ObjectType createObjectType() {
    return ObjectType.builder()
        .name("Building")
        .property(Attribute.builder()
            .identifier(true)
            .name("id")
            .type(ScalarTypes.INTEGER)
            .build())
        .property(Attribute.builder()
            .name("name")
            .type(ScalarTypes.STRING)
            .build())
        .build();
  }

  private static ObjectNode createObjectNode() {
    return new ObjectMapper()
        .createObjectNode()
        .put("id", 1)
        .put("name", "Building 1");
  }

  private static Stream<Arguments> pathsWithExtension() {
    return Stream.of(
        Arguments.of("Foo.json", "Foo"),
        Arguments.of("Foo.Bar.json", "Foo.Bar"),
        Arguments.of("foo/bar/Foo.json", "Foo"),
        Arguments.of("foo/bar/Foo.Bar.json", "Foo.Bar"),
        Arguments.of("/foo/bar/Foo.json", "Foo"),
        Arguments.of("/foo/bar/Foo.Bar.json", "Foo.Bar"));
  }
}
