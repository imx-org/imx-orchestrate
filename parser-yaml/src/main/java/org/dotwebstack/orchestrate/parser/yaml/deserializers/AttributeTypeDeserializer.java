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
    var node = parser.getCodec()
        .readTree(parser);

    // @TODO: Dynamic types, inclusing extension types
    if (node instanceof TextNode) {
      return ScalarTypes.STRING;
    }

    throw new YamlModelParserException(String.format(INVALID_TEXT_NODE, "type"));
  }
}
