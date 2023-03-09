package org.dotwebstack.orchestrate.source;

import lombok.Builder;
import lombok.Getter;
import org.dotwebstack.orchestrate.model.PropertyPath;

@Getter
@Builder(toBuilder = true)
public final class FilterExpression {

  private final PropertyPath propertyPath;

  private final Object value;

  @Override
  public String toString() {
    return propertyPath.toString()
        .concat(" = ")
        .concat(value.toString());
  }
}
