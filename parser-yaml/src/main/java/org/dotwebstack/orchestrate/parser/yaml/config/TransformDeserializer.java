package org.dotwebstack.orchestrate.parser.yaml.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.io.IOException;
import java.io.Serial;
import java.util.Map;
import org.dotwebstack.orchestrate.model.transforms.Transform;
import org.dotwebstack.orchestrate.model.MappingRegistry;
import org.dotwebstack.orchestrate.parser.yaml.YamlModelMappingParserException;

public class TransformDeserializer extends StdDeserializer<Transform> {

  @Serial
  private static final long serialVersionUID = -3632511025789813533L;

  private final MappingRegistry mappingRegistry;

  public TransformDeserializer(MappingRegistry mappingRegistry) {
    super(Transform.class);
    this.mappingRegistry = mappingRegistry;
  }

  @Override
  @SuppressWarnings("unchecked")
  public Transform deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
      throws IOException {

    var token = jsonParser.getCurrentToken();
    if (token == JsonToken.VALUE_STRING) {
      var transformNameValue = (String) deserializationContext.findRootValueDeserializer(deserializationContext.constructType(String.class))
          .deserialize(jsonParser, deserializationContext);

      return mappingRegistry.getTransform(transformNameValue);
    } else if (token == JsonToken.START_OBJECT) {

      var transformMapValue = (Map<String, Object>) deserializationContext.findRootValueDeserializer(
              deserializationContext.constructType(Map.class))
          .deserialize(jsonParser, deserializationContext);

      if (!transformMapValue.containsKey("name")) {
        throw new YamlModelMappingParserException(
            String.format("Transform mapping is missing name property. %s", transformMapValue));
      }

      return mappingRegistry.getTransform((String) transformMapValue.get("name"));
    } else {
      throw new YamlModelMappingParserException(String.format("Error deserializing transform on token %s", token));
    }
  }
}
