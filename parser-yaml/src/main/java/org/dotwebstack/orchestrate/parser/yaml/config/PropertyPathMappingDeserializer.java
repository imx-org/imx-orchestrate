package org.dotwebstack.orchestrate.parser.yaml.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.ResolvableDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.io.IOException;
import java.io.Serial;
import org.dotwebstack.orchestrate.model.PropertyPath;
import org.dotwebstack.orchestrate.model.PropertyPathMapping;

public class PropertyPathMappingDeserializer extends StdDeserializer<PropertyPathMapping> implements
    ResolvableDeserializer {

  @Serial
  private static final long serialVersionUID = 2089408371000624220L;
  private final transient JsonDeserializer<?> defaultDeserializer;

  public PropertyPathMappingDeserializer(JsonDeserializer<?> jsonDeserializer) {
    super(PropertyPathMapping.class);
    this.defaultDeserializer = jsonDeserializer;
  }

  @Override
  public PropertyPathMapping deserialize(JsonParser jsonParser, DeserializationContext ctxt)
      throws IOException {
    var token = jsonParser.getCurrentToken();
    if (token == JsonToken.VALUE_STRING) {
      var stringPathValue = (String) ctxt.findRootValueDeserializer(ctxt.constructType(String.class))
          .deserialize(jsonParser, ctxt);
      return PropertyPathMapping.builder()
          .path(PropertyPath.fromString(stringPathValue))
          .build();
    } else {
      return (PropertyPathMapping) defaultDeserializer.deserialize(jsonParser, ctxt);
    }
  }

  @Override
  public void resolve(DeserializationContext ctxt) throws JsonMappingException {
    ((ResolvableDeserializer) defaultDeserializer).resolve(ctxt);
  }
}
