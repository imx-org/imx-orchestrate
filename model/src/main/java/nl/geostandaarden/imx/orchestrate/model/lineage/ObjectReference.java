package nl.geostandaarden.imx.orchestrate.model.lineage;

import static nl.geostandaarden.imx.orchestrate.model.ModelUtils.extractKey;

import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import nl.geostandaarden.imx.orchestrate.model.ObjectResult;

@Getter
@Builder(toBuilder = true)
public class ObjectReference {

  private final String objectType;

  private final Map<String, Object> objectKey;

  public static ObjectReference fromResult(ObjectResult result) {
    var objectType = result.getType();

    return ObjectReference.builder()
        .objectType(objectType.getName())
        .objectKey(extractKey(objectType, result.getProperties()))
        .build();
  }
}
