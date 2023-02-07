package org.dotwebstack.orchestrate.parser.yaml.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.io.IOException;
import java.io.Serial;
import java.util.Map;
import org.dotwebstack.orchestrate.model.transforms.Transform;
import org.dotwebstack.orchestrate.model.transforms.TransformRegistry;
import org.dotwebstack.orchestrate.parser.yaml.YamlModelMappingParserException;

public class TransformDeserializer extends StdDeserializer<Transform> {

  @Serial
  private static final long serialVersionUID = -3632511025789813533L;

  private final TransformRegistry transformRegistry;

  public TransformDeserializer(TransformRegistry transformRegistry) {
    super(Transform.class);
    this.transformRegistry = transformRegistry;
  }

  @Override
  @SuppressWarnings("unchecked")
  public Transform deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
      throws IOException {

    var token = jsonParser.getCurrentToken();
    if (token == JsonToken.VALUE_STRING) {
      var transformNameValue = (String) deserializationContext.findRootValueDeserializer(deserializationContext.constructType(String.class))
          .deserialize(jsonParser, deserializationContext);

      return transformRegistry.getTransform(transformNameValue);
    } else if (token == JsonToken.START_OBJECT) {

      var transformMapValue = (Map<String, Object>) deserializationContext.findRootValueDeserializer(
              deserializationContext.constructType(Map.class))
          .deserialize(jsonParser, deserializationContext);

      if (!transformMapValue.containsKey("name")) {
        throw new YamlModelMappingParserException(
            String.format("Transform mapping is missing name property. %s", transformMapValue));
      }

      return transformRegistry.getTransform((String) transformMapValue.get("name"));
    } else {
      throw new YamlModelMappingParserException(String.format("Error deserializing transform on token %s", token));
    }
  }
}
