package org.dotwebstack.orchestrate.parser.yaml.deserializers;

import static org.dotwebstack.orchestrate.parser.yaml.YamlModelParser.ATTRIBUTES_KEY;
import static org.dotwebstack.orchestrate.parser.yaml.YamlModelParser.INVALID_OBJECT_NODE;
import static org.dotwebstack.orchestrate.parser.yaml.YamlModelParser.OBJECT_TYPES_KEY;
import static org.dotwebstack.orchestrate.parser.yaml.YamlModelParser.RELATIONS_KEY;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.io.Serial;
import java.util.Map;
import org.dotwebstack.orchestrate.model.Attribute;
import org.dotwebstack.orchestrate.model.ObjectType;
import org.dotwebstack.orchestrate.model.Relation;
import org.dotwebstack.orchestrate.parser.yaml.YamlModelParserException;

public class ObjectTypeDeserializer extends StdDeserializer<ObjectType> {

  @Serial
  private static final long serialVersionUID = 8168580685904926025L;

  public ObjectTypeDeserializer() {
    super(ObjectType.class);
  }

  @Override
  public ObjectType deserialize(JsonParser parser, DeserializationContext context) throws IOException {
    var node = parser.getCodec()
        .readTree(parser);

    if (node instanceof ObjectNode objectNode) {
      return createObjectType(objectNode, context);
    }

    throw new YamlModelParserException(String.format(INVALID_OBJECT_NODE, OBJECT_TYPES_KEY));
  }

  private ObjectType createObjectType(ObjectNode objectNode, DeserializationContext context) {
    var objectTypeBuilder = ObjectType.builder();

    if (objectNode.has(ATTRIBUTES_KEY)) {
      if (objectNode.get(ATTRIBUTES_KEY) instanceof ObjectNode attributesNode) {
        attributesNode.fields()
            .forEachRemaining(entry -> objectTypeBuilder.property(createAttribute(entry, context)));
      } else {
        throw new YamlModelParserException(String.format(INVALID_OBJECT_NODE, ATTRIBUTES_KEY));
      }
    }

    if (objectNode.has(RELATIONS_KEY)) {
      if (objectNode.get(RELATIONS_KEY) instanceof ObjectNode relationsNode) {
        relationsNode.fields()
            .forEachRemaining(entry -> objectTypeBuilder.property(createRelation(entry, context)));
      } else {
        throw new YamlModelParserException(String.format(INVALID_OBJECT_NODE, RELATIONS_KEY));
      }
    }

    return objectTypeBuilder.build();
  }

  private Attribute createAttribute(Map.Entry<String, JsonNode> entry, DeserializationContext context) {
    try {
      return context.readTreeAsValue(entry.getValue(), Attribute.class)
          .toBuilder()
          .name(entry.getKey())
          .build();
    } catch (IOException e) {
      throw new YamlModelParserException("Could not parse attribute: " + entry.getKey(), e);
    }
  }

  private Relation createRelation(Map.Entry<String, JsonNode> entry, DeserializationContext context) {
    try {
      return context.readTreeAsValue(entry.getValue(), Relation.class)
          .toBuilder()
          .name(entry.getKey())
          .build();
    } catch (IOException e) {
      throw new YamlModelParserException("Could not parse relation: " + entry.getKey(), e);
    }
  }
}
