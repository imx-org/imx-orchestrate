package org.dotwebstack.orchestrate.source.file;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.file.Paths;
import java.util.List;
import org.dotwebstack.orchestrate.model.Attribute;
import org.dotwebstack.orchestrate.model.Model;
import org.dotwebstack.orchestrate.model.ObjectType;
import org.dotwebstack.orchestrate.model.ObjectTypeRef;
import org.dotwebstack.orchestrate.model.Relation;
import org.dotwebstack.orchestrate.model.types.ScalarTypes;
import org.dotwebstack.orchestrate.source.CollectionRequest;
import org.dotwebstack.orchestrate.source.SelectedProperty;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.test.StepVerifier;

class FileSourceTest {

  private static final Model MODEL = createModel();

  @Test
  void constructor_PopulatesRepository_ForValidFolder() {
    var folderPath = Paths.get("src/test/resources/source1");
    var fileSource = new FileSource(MODEL, folderPath);
    assertThat(fileSource).isNotNull();

    var objectType = MODEL.getObjectType("Building");
    var objectFlux = fileSource.getDataRepository()
        .find(CollectionRequest.builder()
        .objectType(objectType)
        .selectedProperties(List.of(
            new SelectedProperty(objectType.getProperty("id")),
            new SelectedProperty(objectType.getProperty("name"))))
        .build());

    StepVerifier.create(objectFlux)
        .expectNextCount(3)
        .verifyComplete();
  }

  @ParameterizedTest
  @ValueSource(strings = {"src/test/resources/source", "src/test/resources/source1/Address.json"})
  void constructor_ThrowsException_ForNonExistingOrFilePath(String path) {
    var folderPath = Paths.get(path);
    assertThatThrownBy(() -> new FileSource(MODEL, folderPath)).hasMessage("File path does not exist or is not a " +
        "directory.");
  }

  private static Model createModel() {
    return Model.builder()
        .objectType(ObjectType.builder()
            .name("Building")
            .property(Attribute.builder()
                .identifier(true)
                .name("id")
                .type(ScalarTypes.STRING)
                .build())
            .property(Attribute.builder()
                .name("name")
                .type(ScalarTypes.STRING)
                .build())
            .property(Relation.builder()
                .name("address")
                .target(ObjectTypeRef.forType("Address"))
                .build())
            .build())
        .objectType(ObjectType.builder()
            .name("Address")
            .property(Attribute.builder()
                .identifier(true)
                .name("id")
                .type(ScalarTypes.STRING)
                .build())
            .property(Attribute.builder()
                .name("name")
                .type(ScalarTypes.STRING)
                .build())
            .build())
        .build();
  }
}
