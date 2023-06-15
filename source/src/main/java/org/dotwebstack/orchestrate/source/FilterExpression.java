package org.dotwebstack.orchestrate.source;

import lombok.Builder;
import lombok.Getter;
import org.dotwebstack.orchestrate.model.Path;
import org.dotwebstack.orchestrate.model.filters.FilterOperator;

@Getter
@Builder(toBuilder = true)
public final class FilterExpression {

  private final Path path;

  private final FilterOperator operator;

  private final Object value;

  @Override
  public String toString() {
    return path.toString()
        .concat(" ")
        .concat(operator.toString())
        .concat(" ")
        .concat(value.toString());
  }
}
