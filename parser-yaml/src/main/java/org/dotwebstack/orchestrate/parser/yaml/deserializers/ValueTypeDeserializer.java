package org.dotwebstack.orchestrate.parser.yaml.deserializers;

import static org.dotwebstack.orchestrate.parser.yaml.YamlModelParser.INVALID_TEXT_NODE;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import java.io.IOException;
import java.io.Serial;
import java.util.Map;
import java.util.Optional;
import org.dotwebstack.orchestrate.model.types.ValueType;
import org.dotwebstack.orchestrate.model.types.ValueTypeRegistry;
import org.dotwebstack.orchestrate.parser.yaml.YamlModelParserException;

public class ValueTypeDeserializer extends StdDeserializer<ValueType> {

  @Serial
  private static final long serialVersionUID = 8168580685904926025L;

  private final transient ValueTypeRegistry valueTypeRegistry;

  public ValueTypeDeserializer(ValueTypeRegistry valueTypeRegistry) {
    super(ValueType.class);
    this.valueTypeRegistry = valueTypeRegistry;
  }

  @Override
  public ValueType deserialize(JsonParser parser, DeserializationContext context) throws IOException {
    var typeNode = parser.getCodec()
        .readTree(parser);

    if (typeNode instanceof TextNode textNode) {
      return valueTypeRegistry.getValueTypeFactory(textNode.textValue())
          .create(Map.of());
    }

    if (typeNode instanceof ObjectNode objectNode) {
      var typeName = objectNode.get("name")
          .textValue();

      var options = Optional.ofNullable(objectNode.get("options"))
          .map(optionsNode -> {
            try {
              return context.readTreeAsValue(optionsNode, Map.class);
            } catch (IOException e) {
              throw new YamlModelParserException("Could not parse options map.", e);
            }
          })
          .orElse(Map.of());

      return valueTypeRegistry.getValueTypeFactory(typeName)
          .create(options);
    }

    throw new YamlModelParserException(String.format(INVALID_TEXT_NODE, "type"));
  }
}
