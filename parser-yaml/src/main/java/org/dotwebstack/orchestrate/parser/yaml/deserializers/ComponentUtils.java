package org.dotwebstack.orchestrate.parser.yaml.deserializers;

import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Map;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.dotwebstack.orchestrate.parser.yaml.YamlModelMappingParserException;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class ComponentUtils {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  public static String parseType(TreeNode node) {
    if (node instanceof ObjectNode objectNode) {
      var typeNode = Optional.ofNullable(objectNode.get("type"))
          .orElseThrow(() -> new YamlModelMappingParserException("Result mapper instances require a 'type' property."));

      if (typeNode.getNodeType() != JsonNodeType.STRING) {
        throw new YamlModelMappingParserException("Result mapper `type` property must be a string.");
      }

      return typeNode.textValue();
    }

    throw new YamlModelMappingParserException("Object node expected for component instances.");
  }

  public static Map<String, Object> parseOptions(TreeNode node) {
    return Optional.ofNullable(node.get("options"))
        .map(optionsNode -> OBJECT_MAPPER.convertValue(optionsNode, new TypeReference<Map<String, Object>>() {}))
        .orElse(Map.of());
  }
}
