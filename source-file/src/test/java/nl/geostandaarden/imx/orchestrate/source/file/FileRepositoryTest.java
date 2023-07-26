package nl.geostandaarden.imx.orchestrate.source.file;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.List;
import java.util.Map;
import nl.geostandaarden.imx.orchestrate.model.Attribute;
import nl.geostandaarden.imx.orchestrate.model.ObjectType;
import nl.geostandaarden.imx.orchestrate.model.types.ScalarTypes;
import nl.geostandaarden.imx.orchestrate.source.CollectionRequest;
import nl.geostandaarden.imx.orchestrate.source.ObjectRequest;
import nl.geostandaarden.imx.orchestrate.source.SelectedProperty;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

class FileRepositoryTest {

  private static final ObjectNode BUILDING1_NODE = new ObjectMapper()
      .createObjectNode()
      .put("id", 1)
      .put("name", "Building 1");

  private static final ObjectNode BUILDING2_NODE = new ObjectMapper()
      .createObjectNode()
      .put("id", 2)
      .put("name", "Building 2");

  private static final Map<String, Object> BUILDING1_MAP = Map.of("id", 1, "name", "Building 1");

  private static final Map<String, Object> BUILDING2_MAP = Map.of("id", 2, "name", "Building 2");

  private final FileRepository fileRepository = new FileRepository();

  public FileRepositoryTest() {
    fileRepository.add("Building", Map.of("id", 1), BUILDING1_NODE);
    fileRepository.add("Building", Map.of("id", 2), BUILDING2_NODE);
  }

  @Test
  void findOne_ReturnsObject_IfExists() {
    var objectType = createObjectType();
    var objectRequest = ObjectRequest.builder()
        .objectType(objectType)
        .objectKey(Map.of("id", 1))
        .selectedProperties(List.of(
            new SelectedProperty(objectType.getProperty("id")),
            new SelectedProperty(objectType.getProperty("name"))))
        .build();
    var result = fileRepository.findOne(objectRequest);

    StepVerifier.create(result)
        .expectNext(BUILDING1_MAP)
        .verifyComplete();
  }

  @Test
  void findOne_ReturnsEmptyMono_IfNotExists() {
    var objectType = createObjectType();
    var objectRequest = ObjectRequest.builder()
        .objectType(objectType)
        .objectKey(Map.of("id", 123))
        .selectedProperties(List.of(
            new SelectedProperty(objectType.getProperty("id")),
            new SelectedProperty(objectType.getProperty("name"))))
        .build();
    var result = fileRepository.findOne(objectRequest);

    StepVerifier.create(result)
        .verifyComplete();
  }

  @Test
  void find_ReturnsObjects_ForAllObjects() {
    var objectType = createObjectType();
    var collectionRequest = CollectionRequest.builder()
        .objectType(objectType)
        .selectedProperties(List.of(
            new SelectedProperty(objectType.getProperty("id")),
            new SelectedProperty(objectType.getProperty("name"))))
        .build();
    var result = fileRepository.find(collectionRequest);

    StepVerifier.create(result)
        .expectNext(BUILDING1_MAP)
        .expectNext(BUILDING2_MAP)
        .verifyComplete();
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
}
