package nl.geostandaarden.imx.orchestrate.parser.yaml.deserializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.io.Serial;
import nl.geostandaarden.imx.orchestrate.model.Model;
import nl.geostandaarden.imx.orchestrate.model.ObjectType;
import nl.geostandaarden.imx.orchestrate.parser.yaml.YamlModelParser;
import nl.geostandaarden.imx.orchestrate.parser.yaml.YamlModelParserException;

public class ModelDeserializer extends StdDeserializer<Model> {

    @Serial
    private static final long serialVersionUID = 8168580685904926025L;

    public ModelDeserializer() {
        super(Model.class);
    }

    @Override
    public Model deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        var modelNode = parser.getCodec().readTree(parser);

        if (!modelNode.isObject()) {
            throw new YamlModelParserException("Model is not an object node.");
        }

        if (modelNode.get(YamlModelParser.OBJECT_TYPES_KEY) instanceof ObjectNode objectTypesNode) {
            var modelBuilder = Model.builder();

            objectTypesNode.fields().forEachRemaining(entry -> {
                try {
                    modelBuilder.objectType(context.readTreeAsValue(entry.getValue(), ObjectType.class).toBuilder()
                            .name(entry.getKey())
                            .build());
                } catch (IOException e) {
                    throw new YamlModelParserException("Could not parse object type: " + entry.getKey(), e);
                }
            });

            return modelBuilder.build();
        }

        throw new YamlModelParserException(
                String.format(YamlModelParser.INVALID_OBJECT_NODE, YamlModelParser.OBJECT_TYPES_KEY));
    }
}
