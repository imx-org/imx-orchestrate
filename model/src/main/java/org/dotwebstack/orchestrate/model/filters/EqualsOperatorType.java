package org.dotwebstack.orchestrate.model.filters;

import java.util.Map;

public final class EqualsOperatorType implements FilterOperatorType {

  @Override
  public String getName() {
    return "equals";
  }

  @Override
  public FilterOperator create(Map<String, Object> options) {
    return EqualsOperator.builder()
        .type(getName())
        .build();
  }
}
