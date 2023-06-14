package org.dotwebstack.orchestrate.source;

import java.util.Map;
import java.util.function.Function;
import lombok.Builder;
import org.dotwebstack.orchestrate.model.Path;
import org.dotwebstack.orchestrate.model.filters.EqualsOperatorType;
import org.dotwebstack.orchestrate.model.filters.FilterOperator;

@Builder(toBuilder = true)
public final class FilterDefinition {

  private final Path path;

  @Builder.Default
  private final FilterOperator operator = new EqualsOperatorType().create(Map.of());

  private final Function<Map<String, Object>, Object> valueExtractor;

  public FilterExpression createExpression(Map<String, Object> input) {
    return FilterExpression.builder()
        .path(path)
        .operator(operator)
        .value(valueExtractor.apply(input))
        .build();
  }
}
