package org.dotwebstack.orchestrate.model.combiners;

import java.util.Map;
import java.util.Objects;

public final class Coalesce implements ResultCombinerType {

  @Override
  public String getName() {
    return "coalesce";
  }

  @Override
  public ResultCombiner create(Map<String, Object> options) {
    return result -> result.stream()
        .filter(Objects::nonNull)
        .findFirst()
        .orElse(null);
  }
}
