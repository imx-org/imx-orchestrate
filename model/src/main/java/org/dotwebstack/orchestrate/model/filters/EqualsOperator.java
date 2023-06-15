package org.dotwebstack.orchestrate.model.filters;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(toBuilder = true)
public class EqualsOperator implements FilterOperator {

  private final String type;

  @Override
  public String toString() {
    return "=";
  }
}
