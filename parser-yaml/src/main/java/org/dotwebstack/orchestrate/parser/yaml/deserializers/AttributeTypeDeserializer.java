package org.dotwebstack.orchestrate.parser.yaml.deserializers;

import static org.dotwebstack.orchestrate.parser.yaml.YamlModelParser.INVALID_TEXT_NODE;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.TextNode;
import java.io.IOException;
import java.io.Serial;
import org.dotwebstack.orchestrate.model.AttributeType;
import org.dotwebstack.orchestrate.model.types.ScalarTypes;
import org.dotwebstack.orchestrate.parser.yaml.YamlModelParserException;

public class AttributeTypeDeserializer extends StdDeserializer<AttributeType> {

  @Serial
  private static final long serialVersionUID = 8168580685904926025L;

  public AttributeTypeDeserializer() {
    super(AttributeType.class);
  }

  @Override
  public AttributeType deserialize(JsonParser parser, DeserializationContext context) throws IOException {
    var typeNode = parser.getCodec()
        .readTree(parser);

    // @TODO: Make more dynamic + include extension types
    if (typeNode instanceof TextNode textNode) {
      return switch (textNode.textValue()) {
        case "Boolean" -> ScalarTypes.STRING;
        case "Double" -> ScalarTypes.DOUBLE;
        case "Float" -> ScalarTypes.FLOAT;
        case "Integer" -> ScalarTypes.INTEGER;
        case "Long" -> ScalarTypes.LONG;
        case "String" -> ScalarTypes.STRING;
        default -> throw new YamlModelParserException("Unknown attribute type: " + textNode.textValue());
      };
    }

    throw new YamlModelParserException(String.format(INVALID_TEXT_NODE, "type"));
  }
}
