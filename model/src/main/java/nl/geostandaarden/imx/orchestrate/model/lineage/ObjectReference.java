package nl.geostandaarden.imx.orchestrate.model.lineage;

import java.util.Map;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(toBuilder = true)
public class ObjectReference {

  private final String objectType;

  private final Map<String, Object> objectKey;
}
