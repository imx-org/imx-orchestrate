package org.dotwebstack.orchestrate.model.matchers;

import java.util.Map;
import java.util.Objects;

public class NotNullType implements MatcherType {

  @Override
  public String getName() {
    return "notNull";
  }

  @Override
  public Matcher create(Map<String, Object> options) {
    return Objects::nonNull;
  }
}
