package nl.geostandaarden.imx.orchestrate.parser.yaml.deserializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.io.Serial;
import java.util.Map;
import nl.geostandaarden.imx.orchestrate.model.Attribute;
import nl.geostandaarden.imx.orchestrate.model.ObjectType;
import nl.geostandaarden.imx.orchestrate.model.Relation;
import nl.geostandaarden.imx.orchestrate.parser.yaml.YamlModelParser;
import nl.geostandaarden.imx.orchestrate.parser.yaml.YamlModelParserException;

public class ObjectTypeDeserializer extends StdDeserializer<ObjectType> {

    @Serial
    private static final long serialVersionUID = 8168580685904926025L;

    public ObjectTypeDeserializer() {
        super(ObjectType.class);
    }

    @Override
    public ObjectType deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        var node = parser.getCodec().readTree(parser);

        if (node instanceof ObjectNode objectTypeNode) {
            return createObjectType(objectTypeNode, context);
        }

        throw new YamlModelParserException(
                String.format(YamlModelParser.INVALID_OBJECT_NODE, YamlModelParser.OBJECT_TYPES_KEY));
    }

    private ObjectType createObjectType(ObjectNode objectTypeNode, DeserializationContext context) {
        var objectTypeBuilder = ObjectType.builder();

        if (objectTypeNode.has(YamlModelParser.ATTRIBUTES_KEY)) {
            if (objectTypeNode.get(YamlModelParser.ATTRIBUTES_KEY) instanceof ObjectNode attributesNode) {
                attributesNode
                        .fields()
                        .forEachRemaining(entry -> objectTypeBuilder.property(createAttribute(entry, context)));
            } else {
                throw new YamlModelParserException(
                        String.format(YamlModelParser.INVALID_OBJECT_NODE, YamlModelParser.ATTRIBUTES_KEY));
            }
        }

        if (objectTypeNode.has(YamlModelParser.RELATIONS_KEY)) {
            if (objectTypeNode.get(YamlModelParser.RELATIONS_KEY) instanceof ObjectNode relationsNode) {
                relationsNode
                        .fields()
                        .forEachRemaining(entry -> objectTypeBuilder.property(createRelation(entry, context)));
            } else {
                throw new YamlModelParserException(
                        String.format(YamlModelParser.INVALID_OBJECT_NODE, YamlModelParser.RELATIONS_KEY));
            }
        }

        return objectTypeBuilder.build();
    }

    private Attribute createAttribute(Map.Entry<String, JsonNode> entry, DeserializationContext context) {
        try {
            return context.readTreeAsValue(entry.getValue(), Attribute.class).toBuilder()
                    .name(entry.getKey())
                    .build();
        } catch (IOException e) {
            throw new YamlModelParserException("Could not parse attribute: " + entry.getKey(), e);
        }
    }

    private Relation createRelation(Map.Entry<String, JsonNode> entry, DeserializationContext context) {
        try {
            return context.readTreeAsValue(entry.getValue(), Relation.class).toBuilder()
                    .name(entry.getKey())
                    .build();
        } catch (IOException e) {
            throw new YamlModelParserException("Could not parse relation: " + entry.getKey(), e);
        }
    }
}
