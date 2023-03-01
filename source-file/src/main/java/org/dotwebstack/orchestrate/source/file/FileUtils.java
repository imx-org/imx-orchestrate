package org.dotwebstack.orchestrate.source.file;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.dotwebstack.orchestrate.model.ObjectType;
import org.dotwebstack.orchestrate.model.Property;
import org.dotwebstack.orchestrate.source.SelectedProperty;
import org.dotwebstack.orchestrate.source.SourceException;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FileUtils {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  public static String getBaseName(Path filePath) {
    var fileName = filePath.getFileName()
        .toString();

    var dotIndex = fileName.lastIndexOf(".");

    if (dotIndex == -1) {
      throw new IllegalArgumentException("File path does not have an extension.");
    }

    return fileName.substring(0, dotIndex);
  }

  public static Map<String, Object> getObjectKey(ObjectNode objectNode, ObjectType objectType) {
    var propertyNames = objectType.getIdentityProperties()
        .stream()
        .map(Property::getName)
        .toList();

    return objectNodeToMap(objectNode.retain(propertyNames));
  }

  public static Map<String, Object> getObjectProperties(ObjectNode objectNode,
      List<SelectedProperty> selectedProperties) {
    var propertyNames = selectedProperties.stream()
        .map(SelectedProperty::getProperty)
        .map(Property::getName)
        .toList();

    return objectNodeToMap(objectNode.retain(propertyNames));
  }

  @SuppressWarnings("unchecked")
  private static Map<String, Object> objectNodeToMap(ObjectNode objectNode) {
    try {
      return OBJECT_MAPPER.treeToValue(objectNode, Map.class);
    } catch (JsonProcessingException e) {
      throw new SourceException("Error while procession object.", e);
    }
  }
}
