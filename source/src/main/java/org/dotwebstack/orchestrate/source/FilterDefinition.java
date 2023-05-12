package org.dotwebstack.orchestrate.source;

import java.util.Map;
import java.util.function.Function;
import lombok.Builder;
import org.dotwebstack.orchestrate.model.Path;

@Builder(toBuilder = true)
public final class FilterDefinition {

  private final Path path;

  private final Function<Map<String, Object>, Object> valueExtractor;

  public FilterExpression createExpression(Map<String, Object> input) {
    return FilterExpression.builder()
        .path(path)
        .value(valueExtractor.apply(input))
        .build();
  }
}
