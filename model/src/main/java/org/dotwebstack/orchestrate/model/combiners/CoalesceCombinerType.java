package org.dotwebstack.orchestrate.model.combiners;

import static java.util.Collections.emptySet;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.dotwebstack.orchestrate.model.PathResult;
import org.dotwebstack.orchestrate.model.PropertyResult;

public final class CoalesceCombinerType implements ResultCombinerType {

  @Override
  public String getName() {
    return "coalesce";
  }

  @Override
  public ResultCombiner create(Map<String, Object> options) {
    return pathResults -> pathResults.stream()
        .filter(PathResult::notEmpty)
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
