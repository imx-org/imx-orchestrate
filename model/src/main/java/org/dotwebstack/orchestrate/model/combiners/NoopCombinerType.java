package org.dotwebstack.orchestrate.model.combiners;

import static java.util.stream.Collectors.toSet;

import java.util.Map;
import org.dotwebstack.orchestrate.model.PathResult;
import org.dotwebstack.orchestrate.model.PropertyResult;

public final class NoopCombinerType implements ResultCombinerType {

  @Override
  public String getName() {
    return "noop";
  }

  @Override
  public ResultCombiner create(Map<String, Object> options) {
    return pathResults -> {
      var nonEmptyResults = pathResults
          .stream()
          .filter(PathResult::isNotNull)
          .toList();

      return PropertyResult.builder()
          .value(nonEmptyResults.stream()
              .map(PathResult::getValue)
              .toList())
          .sourceProperties(nonEmptyResults.stream()
              .map(PathResult::getSourceProperty)
              .collect(toSet()))
          .build();
    };
  }
}
