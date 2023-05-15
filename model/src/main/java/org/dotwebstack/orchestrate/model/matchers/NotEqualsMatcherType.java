package org.dotwebstack.orchestrate.model.matchers;

import java.util.Collection;
import java.util.Map;

public final class NotEqualsMatcherType implements MatcherType {

  @Override
  public String getName() {
    return "notEquals";
  }

  @Override
  public Matcher create(Map<String, Object> options) {
    var value = options.get("value");

    return input -> {
      if (value instanceof Collection<?> collectionValue) {
        return !collectionValue.contains(input);
      }

      return !value.equals(input);
    };
  }
}
