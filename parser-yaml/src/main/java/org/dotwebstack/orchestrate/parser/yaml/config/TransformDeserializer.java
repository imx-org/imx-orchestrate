package org.dotwebstack.orchestrate.parser.yaml.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.io.IOException;
import java.io.Serial;
import java.util.Map;
import org.dotwebstack.orchestrate.model.transforms.Transform;
import org.dotwebstack.orchestrate.model.ComponentRegistry;
import org.dotwebstack.orchestrate.parser.yaml.YamlModelMappingParserException;

public class TransformDeserializer extends StdDeserializer<Transform> {

  @Serial
  private static final long serialVersionUID = -3632511025789813533L;

  private final ComponentRegistry componentRegistry;

  public TransformDeserializer(ComponentRegistry componentRegistry) {
    super(Transform.class);
    this.componentRegistry = componentRegistry;
  }

  @Override
  @SuppressWarnings("unchecked")
  public Transform deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
      throws IOException {

    var token = jsonParser.getCurrentToken();
    if (token == JsonToken.VALUE_STRING) {
      var transformNameValue = (String) deserializationContext.findRootValueDeserializer(deserializationContext.constructType(String.class))
          .deserialize(jsonParser, deserializationContext);

      return componentRegistry.getTransform(transformNameValue);
    } else if (token == JsonToken.START_OBJECT) {

      var transformMapValue = (Map<String, Object>) deserializationContext.findRootValueDeserializer(
              deserializationContext.constructType(Map.class))
          .deserialize(jsonParser, deserializationContext);

      if (!transformMapValue.containsKey("name")) {
        throw new YamlModelMappingParserException(
            String.format("Transform mapping is missing name property. %s", transformMapValue));
      }

      return componentRegistry.getTransform((String) transformMapValue.get("name"));
    } else {
      throw new YamlModelMappingParserException(String.format("Error deserializing transform on token %s", token));
    }
  }
}
