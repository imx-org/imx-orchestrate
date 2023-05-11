package org.dotwebstack.orchestrate.model.combiners;

import java.util.Map;
import java.util.stream.Collectors;

public final class Join implements ResultCombinerType {

  @Override
  public String getName() {
    return "join";
  }

  @Override
  public ResultCombiner create(Map<String, Object> options) {
    return result -> {
      if (result == null) {
        return null;
      }

      return result.stream()
          .map(String::valueOf)
          .collect(Collectors.joining());
    };
  }
}
