package nl.geostandaarden.imx.orchestrate.model.matchers;

import java.util.Map;
import java.util.Objects;

public final class NotNullMatcherType implements MatcherType {

  @Override
  public String getName() {
    return "notNull";
  }

  @Override
  public Matcher create(Map<String, Object> options) {
    return Objects::nonNull;
  }
}
