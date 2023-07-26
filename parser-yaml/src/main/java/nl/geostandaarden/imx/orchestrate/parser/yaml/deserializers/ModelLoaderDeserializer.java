package nl.geostandaarden.imx.orchestrate.parser.yaml.deserializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.io.Serial;
import java.util.Optional;
import nl.geostandaarden.imx.orchestrate.model.Model;
import nl.geostandaarden.imx.orchestrate.model.loader.ModelLoaderRegistry;
import nl.geostandaarden.imx.orchestrate.model.types.ValueTypeRegistry;
import nl.geostandaarden.imx.orchestrate.parser.yaml.YamlModelMappingParserException;

public class ModelLoaderDeserializer extends StdDeserializer<Model> {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @Serial
  private static final long serialVersionUID = 2089408371000624220L;

  private final transient ModelLoaderRegistry modelLoaderRegistry;

  private final transient ValueTypeRegistry valueTypeRegistry;

  public ModelLoaderDeserializer(ModelLoaderRegistry modelLoaderRegistry, ValueTypeRegistry valueTypeRegistry) {
    super(Model.class);
    this.modelLoaderRegistry = modelLoaderRegistry;
    this.valueTypeRegistry = valueTypeRegistry;
  }

  @Override
  public Model deserialize(JsonParser parser, DeserializationContext context) throws IOException {
    var node = parser.getCodec()
        .readTree(parser);

    if (node instanceof ObjectNode modelNode) {
      return resolveModel(modelNode);
    }

    throw new YamlModelMappingParserException("Node describing model is not an object node.");
  }

  private Model resolveModel(ObjectNode modelNode) {
    if (!modelNode.has("location")) {
      throw new YamlModelMappingParserException("Expected property `location` is missing.");
    }

    var loader = Optional.ofNullable(modelNode.get("loader"))
        .map(JsonNode::textValue)
        .orElse("yaml");

    var location = modelNode.get("location")
        .textValue();

    return modelLoaderRegistry.getModelLoader(loader)
        .load(location, valueTypeRegistry);
  }
}
