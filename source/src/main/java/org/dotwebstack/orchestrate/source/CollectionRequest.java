package org.dotwebstack.orchestrate.source;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder(toBuilder = true)
public final class CollectionRequest extends AbstractDataRequest {

  private final FilterExpression filter;

  @Override
  public String toString() {
    return super.toString()
        .concat(filter == null ? "" : "Filter: " + filter + "\n");
  }
}
