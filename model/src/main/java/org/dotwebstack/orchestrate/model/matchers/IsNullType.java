package org.dotwebstack.orchestrate.model.matchers;

import java.util.Map;
import java.util.Objects;

public class IsNullType implements ResultMatcherType {

  @Override
  public String getName() {
    return "isNull";
  }

  @Override
  public ResultMatcher create(Map<String, Object> options) {
    return Objects::isNull;
  }
}
