package org.dotwebstack.orchestrate.model.filters;

import java.util.Map;
import java.util.function.Function;
import lombok.Builder;
import org.dotwebstack.orchestrate.model.Path;

@Builder(toBuilder = true)
public final class FilterDefinition {

  private final Path path;

  @Builder.Default
  private final FilterOperator operator = new EqualsOperatorType().create(Map.of());

  private final Object value;

  private final Function<Map<String, Object>, Object> valueExtractor;

  public FilterExpression createExpression(Map<String, Object> input) {
    return FilterExpression.builder()
        .path(path)
        .operator(operator)
        .value(valueExtractor != null ? valueExtractor.apply(input) : value)
        .build();
  }
}
