package nl.geostandaarden.imx.orchestrate.model.combiners;

import static java.util.Collections.emptySet;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import nl.geostandaarden.imx.orchestrate.model.result.PathResult;
import nl.geostandaarden.imx.orchestrate.model.result.PropertyResult;

public final class CoalesceCombinerType implements ResultCombinerType {

  @Override
  public String getName() {
    return "coalesce";
  }

  @Override
  public ResultCombiner create(Map<String, Object> options) {
    return pathResults -> pathResults.stream()
        .filter(PathResult::isNotNull)
        .map(pathResult -> PropertyResult.builder()
            .value(pathResult.getValue())
            .sourceProperties(Optional.ofNullable(pathResult.getSourceProperty())
                .map(Set::of)
                .orElse(emptySet()))
            .build())
        .findFirst()
        .orElse(PropertyResult.empty());
  }
}
