package nl.geostandaarden.imx.orchestrate.model.matchers;

import java.util.Collection;
import java.util.Map;

public final class EqualsMatcherType implements MatcherType {

  @Override
  public String getName() {
    return "equals";
  }

  @Override
  public Matcher create(Map<String, Object> options) {
    var value = options.get("value");

    return input -> {
      if (value instanceof Collection<?> collectionValue) {
        return collectionValue.contains(input);
      }

      return value.equals(input);
    };
  }
}
