package org.dotwebstack.orchestrate.engine.fetch;

import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import org.dotwebstack.orchestrate.model.ObjectType;

@Getter
@Builder(toBuilder = true)
public class ObjectResult {

  private final ObjectType objectType;

  private final Map<String, Object> objectKey;

  @Singular
  private final Map<String, Object> properties;

  @Singular
  private final Map<String, ObjectResult> nestedObjects;

  public Object getProperty(String name) {
    return properties.get(name);
  }

  public ObjectResult getNestedObject(String name) {
    return nestedObjects.get(name);
  }
}
