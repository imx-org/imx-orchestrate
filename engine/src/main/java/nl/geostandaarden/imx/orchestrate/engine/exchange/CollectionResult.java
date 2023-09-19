package nl.geostandaarden.imx.orchestrate.engine.exchange;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

@Getter
@Builder(toBuilder = true)
public class CollectionResult {

  @Singular
  private final List<ObjectResult> objectResults;
}
