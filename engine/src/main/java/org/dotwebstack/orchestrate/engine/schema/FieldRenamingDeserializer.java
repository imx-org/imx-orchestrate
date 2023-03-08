package org.dotwebstack.orchestrate.engine.schema;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.io.IOException;
import java.io.Serial;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FieldRenamingDeserializer extends StdDeserializer<Object> {

  @Serial
  private static final long serialVersionUID = -4918953459067471487L;
  private final transient JsonDeserializer<?> defaultDeserializer;

  private final Map<String, String> fieldNameMapping;

  public FieldRenamingDeserializer(JsonDeserializer<?> jsonDeserializer, Map<String, String> fieldNameMapping) {
    super(Object.class);
    this.defaultDeserializer = jsonDeserializer;
    this.fieldNameMapping = fieldNameMapping;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Object deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException {
    var token = jsonParser.getCurrentToken();

    if (token == JsonToken.START_ARRAY) {
      var listNode = (List<?>) ctxt.findRootValueDeserializer(ctxt.constructType(List.class))
          .deserialize(jsonParser, ctxt);

      return deserializeListNode(listNode);
    } else if (token == JsonToken.START_OBJECT) {

      var mapNode = (Map<String, Object>) ctxt.findRootValueDeserializer(ctxt.constructType(Map.class))
          .deserialize(jsonParser, ctxt);

      return deserializeMapNode(mapNode);
    } else {
      return defaultDeserializer.deserialize(jsonParser, ctxt);
    }

  }

  @SuppressWarnings("unchecked")
  private Object deserializeListNode(List<?> listNode) {
    return listNode.stream()
        .map(item -> {
          if (item instanceof List) {
            return deserializeListNode((List<?>) item);
          } else if (item instanceof Map) {
            return deserializeMapNode((Map<String, Object>) item);
          }
          return item;
        })
        .toList();
  }

  private Object deserializeMapNode(Map<String, Object> mapNode) {
    return mapNode.entrySet()
        .stream()
        .map(entry -> {
          var fieldName = entry.getKey();
          if (fieldNameMapping.containsKey(fieldName)) {
            return Map.entry(fieldNameMapping.get(fieldName), entry.getValue());
          }
          return entry;
        })
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }
}
