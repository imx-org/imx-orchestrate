package nl.geostandaarden.imx.orchestrate.model.combiners;

import java.util.Map;
import nl.geostandaarden.imx.orchestrate.model.result.PathResult;
import nl.geostandaarden.imx.orchestrate.model.result.PropertyMappingResult;

public final class CoalesceCombinerType implements ResultCombinerType {

  @Override
  public String getName() {
    return "coalesce";
  }

  @Override
  public ResultCombiner create(Map<String, Object> options) {
    return pathResults -> pathResults.stream()
        .filter(PathResult::isNotNull)
        .map(pathResult -> PropertyMappingResult.builder()
            .value(pathResult.getValue())
            .sourceDataElements(pathResult.getPathExecution()
                .getReferences())
            .build())
        .findFirst()
        .orElse(PropertyMappingResult.empty());
  }
}
