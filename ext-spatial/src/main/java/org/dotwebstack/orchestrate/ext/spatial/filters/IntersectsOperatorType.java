package org.dotwebstack.orchestrate.ext.spatial.filters;

import java.util.Map;
import org.dotwebstack.orchestrate.model.filters.FilterOperator;
import org.dotwebstack.orchestrate.model.filters.FilterOperatorType;

public class IntersectsOperatorType implements FilterOperatorType {

  @Override
  public String getName() {
    return "intersects";
  }

  @Override
  public FilterOperator create(Map<String, Object> options) {
    return IntersectsOperator.builder()
        .type(getName())
        .build();
  }
}
