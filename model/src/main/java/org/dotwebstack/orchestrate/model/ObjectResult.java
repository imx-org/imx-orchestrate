package org.dotwebstack.orchestrate.model;

import static org.dotwebstack.orchestrate.model.ModelUtils.keyExtractor;

import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

@Getter
@Builder(toBuilder = true)
public class ObjectResult {

  private final ObjectType type;

  @Singular
  private final Map<String, Object> properties;

  public Map<String, Object> getKey() {
    return keyExtractor(type).apply(this);
  }

  public Object getProperty(String name) {
    return properties.get(name);
  }

  public ObjectResult withProperties(Map<String, Object> properties) {
    return toBuilder()
        .properties(properties)
        .build();
  }
}
