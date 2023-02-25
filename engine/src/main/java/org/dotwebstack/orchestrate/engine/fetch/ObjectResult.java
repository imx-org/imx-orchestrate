package org.dotwebstack.orchestrate.engine.fetch;

import static java.util.Collections.unmodifiableMap;

import java.util.HashMap;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import org.dotwebstack.orchestrate.engine.schema.SchemaConstants;
import org.dotwebstack.orchestrate.model.ObjectType;
import org.dotwebstack.orchestrate.model.lineage.ObjectLineage;

@Getter
@Builder(toBuilder = true)
public class ObjectResult {

  private final ObjectType type;

  @Singular
  private final Map<String, Object> properties;

  @Singular
  private final Map<String, ObjectResult> relatedObjects;

  private final ObjectLineage lineage;

  public Object getProperty(String name) {
    return properties.get(name);
  }

  public ObjectResult getRelatedObject(String name) {
    return relatedObjects.get(name);
  }

  public Map<String, Object> toMap() {
    var resultMap = new HashMap<>(properties);
    relatedObjects.forEach((name, relatedObject) -> resultMap.put(name, relatedObject.toMap()));
    resultMap.put(SchemaConstants.HAS_LINEAGE_FIELD, lineage);
    return unmodifiableMap(resultMap);
  }
}
