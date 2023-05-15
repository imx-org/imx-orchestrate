package org.dotwebstack.orchestrate.model.combiners;

import java.util.Map;
import java.util.stream.Collectors;
import org.dotwebstack.orchestrate.model.PathResult;
import org.dotwebstack.orchestrate.model.PropertyResult;

public class SumType implements ResultCombinerType {

  @Override
  public String getName() {
    return "sum";
  }

  @Override
  public ResultCombiner create(Map<String, Object> options) {
    return pathResults -> {
      // TODO: Support double/decimal types? Improve type safety?
      var sumValue = pathResults.stream()
          .map(PathResult::getValue)
          .map(Integer.class::cast)
          .mapToInt(Integer::intValue).sum();

      return PropertyResult.builder()
          .value(sumValue)
          .sourceProperties(pathResults.stream()
              .map(PathResult::getSourceProperty)
              .collect(Collectors.toSet()))
          .build();
    };
  }
}
