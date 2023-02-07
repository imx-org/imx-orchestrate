package org.dotwebstack.orchestrate.source;

import java.util.Map;
import java.util.function.Function;
import lombok.Builder;
import org.dotwebstack.orchestrate.model.PropertyPath;

@Builder(toBuilder = true)
public final class FilterDefinition {

  private final PropertyPath propertyPath;

  private final Function<Map<String, Object>, Object> valueExtractor;

  public FilterExpression createExpression(Map<String, Object> input) {
    return FilterExpression.builder()
        .propertyPath(propertyPath)
        .value(valueExtractor.apply(input))
        .build();
  }
}
