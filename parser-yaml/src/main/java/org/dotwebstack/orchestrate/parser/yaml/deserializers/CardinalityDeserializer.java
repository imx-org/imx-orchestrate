package org.dotwebstack.orchestrate.parser.yaml.deserializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
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
    var node = parser.getCodec()
        .readTree(parser);

    if (node instanceof TextNode textNode) {
      return Cardinality.fromString(textNode.textValue());
    }

    throw new YamlModelMappingParserException("Cardinality is not a text node.");
  }
}
