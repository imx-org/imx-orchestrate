package nl.geostandaarden.imx.orchestrate.parser.yaml.deserializers;

import static nl.geostandaarden.imx.orchestrate.parser.yaml.YamlModelParser.INVALID_TEXT_OR_INT_NODE;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.TextNode;
import java.io.IOException;
import java.io.Serial;
import nl.geostandaarden.imx.orchestrate.model.Multiplicity;
import nl.geostandaarden.imx.orchestrate.parser.yaml.YamlModelMappingParserException;

public final class MultiplicityDeserializer extends StdDeserializer<Multiplicity> {

    @Serial
    private static final long serialVersionUID = -1800785545802888335L;

    public MultiplicityDeserializer() {
        super(Multiplicity.class);
    }

    @Override
    public Multiplicity deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        var multiplicityNode = parser.getCodec().readTree(parser);

        if (multiplicityNode instanceof TextNode textNode) {
            return Multiplicity.fromString(textNode.textValue());
        }

        if (multiplicityNode instanceof IntNode intNode) {
            return Multiplicity.fromString(intNode.asText());
        }

        throw new YamlModelMappingParserException(String.format(INVALID_TEXT_OR_INT_NODE, "multiplicity"));
    }
}
