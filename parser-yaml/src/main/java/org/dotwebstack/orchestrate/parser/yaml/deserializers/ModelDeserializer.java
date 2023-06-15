package org.dotwebstack.orchestrate.parser.yaml.deserializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.io.Serial;
import java.util.Map;
import org.dotwebstack.orchestrate.model.Model;
import org.dotwebstack.orchestrate.model.loader.ModelLoaderRegistry;
import org.dotwebstack.orchestrate.parser.yaml.YamlModelMappingParserException;

public class ModelDeserializer extends StdDeserializer<Model> {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @Serial
  private static final long serialVersionUID = 2089408371000624220L;

  private final transient ModelLoaderRegistry modelLoaderRegistry;

  public ModelDeserializer(ModelLoaderRegistry modelLoaderRegistry) {
    super(Model.class);
    this.modelLoaderRegistry = modelLoaderRegistry;
  }

  @Override
  public Model deserialize(JsonParser parser, DeserializationContext context) throws IOException {
    var node = parser.getCodec()
        .readTree(parser);

    if (node instanceof ObjectNode) {
      var nodeMap = OBJECT_MAPPER.convertValue(node, new TypeReference<Map<String, Map<String, String>>>() {
      });

      return nodeMap.entrySet()
          .stream()
          .map(modelEntry -> resolveModel(modelEntry.getKey(), modelEntry.getValue()))
          .findFirst()
          .orElse(null);

    } else {
      throw new YamlModelMappingParserException("Node describing model should be a mapping node");
    }
  }

  private Model resolveModel(String alias, Map<String, String> modelSpec) {
    if (!modelSpec.containsKey("profile")) {
      throw new YamlModelMappingParserException(
          String.format("Expected property `profile` is missing in model node: %s", modelSpec));
    }
    if (!modelSpec.containsKey("location")) {
      throw new YamlModelMappingParserException(
          String.format("Expected property `location` is missing in model node: %s", modelSpec));
    }

    var profile = modelSpec.get("profile");
    var location = modelSpec.get("location");

    return modelLoaderRegistry.getModelLoader(profile)
        .loadModel(alias, location)
        .orElseThrow(() -> new YamlModelMappingParserException(
            String.format("Could not resolve model from `%s` for model: %s", location, modelSpec)));
  }
}
