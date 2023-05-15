package org.dotwebstack.orchestrate.model.matchers;

import java.util.Map;
import java.util.Objects;

public class IsNullType implements MatcherType {

  @Override
  public String getName() {
    return "isNull";
  }

  @Override
  public Matcher create(Map<String, Object> options) {
    return Objects::isNull;
  }
}
