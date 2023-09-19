package nl.geostandaarden.imx.orchestrate.engine.exchange;

import static nl.geostandaarden.imx.orchestrate.model.ModelUtils.extractKey;

import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import nl.geostandaarden.imx.orchestrate.model.ObjectType;
import nl.geostandaarden.imx.orchestrate.model.lineage.ObjectReference;

@Getter
@Builder(toBuilder = true)
public class ObjectResult {

  private final ObjectType type;

  @Singular
  private final Map<String, Object> properties;

  public Map<String, Object> getKey() {
    return extractKey(type, properties);
  }

  public Object getProperty(String name) {
    return properties.get(name);
  }

  public ObjectResult withProperties(Map<String, Object> properties) {
    return toBuilder()
        .properties(properties)
        .build();
  }

  public ObjectReference getObjectReference() {
    return ObjectReference.builder()
        .objectType(type.getName())
        .objectKey(extractKey(type, properties))
        .build();
  }
}
