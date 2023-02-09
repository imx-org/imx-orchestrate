package org.dotwebstack.orchestrate.parser.yaml.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.io.IOException;
import java.io.Serial;
import org.dotwebstack.orchestrate.model.ComponentRegistry;
import org.dotwebstack.orchestrate.model.transforms.Transform;

public class TransformDeserializer extends StdDeserializer<Transform> {

  @Serial
  private static final long serialVersionUID = 2089408371000624220L;
  private final transient JsonDeserializer<?> defaultDeserializer;

  private final ComponentRegistry componentRegistry;

  public TransformDeserializer(JsonDeserializer<?> jsonDeserializer, ComponentRegistry componentRegistry) {
    super(Transform.class);
    this.defaultDeserializer = jsonDeserializer;
    this.componentRegistry = componentRegistry;
  }

  @Override
  public Transform deserialize(JsonParser jsonParser, DeserializationContext ctxt)
      throws IOException {
    var token = jsonParser.getCurrentToken();
    if (token == JsonToken.VALUE_STRING) {
      var transformName = (String) ctxt.findRootValueDeserializer(ctxt.constructType(String.class))
          .deserialize(jsonParser, ctxt);
      return componentRegistry.getTransform(transformName);
    } else {
      return (Transform) defaultDeserializer.deserialize(jsonParser, ctxt);
    }
  }
}
