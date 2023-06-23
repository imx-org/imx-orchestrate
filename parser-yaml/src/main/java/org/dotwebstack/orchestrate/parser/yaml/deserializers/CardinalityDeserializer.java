package org.dotwebstack.orchestrate.parser.yaml.deserializers;

import static org.dotwebstack.orchestrate.parser.yaml.YamlModelParser.INVALID_TEXT_OR_INT_NODE;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.TextNode;
import java.io.IOException;
import java.io.Serial;
import org.dotwebstack.orchestrate.model.Cardinality;
import org.dotwebstack.orchestrate.parser.yaml.YamlModelMappingParserException;

public final class CardinalityDeserializer extends StdDeserializer<Cardinality> {

  @Serial
  private static final long serialVersionUID = -1800785545802888335L;

  public CardinalityDeserializer() {
    super(Cardinality.class);
  }

  @Override
  public Cardinality deserialize(JsonParser parser, DeserializationContext context) throws IOException {
    var cardinalityNode = parser.getCodec()
        .readTree(parser);

    if (cardinalityNode instanceof TextNode textNode) {
      return Cardinality.fromString(textNode.textValue());
    }

    if (cardinalityNode instanceof IntNode intNode) {
      return Cardinality.fromString(intNode.asText());
    }

    throw new YamlModelMappingParserException(String.format(INVALID_TEXT_OR_INT_NODE, "cardinality"));
  }
}
