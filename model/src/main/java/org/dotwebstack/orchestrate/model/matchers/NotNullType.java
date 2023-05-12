package org.dotwebstack.orchestrate.model.matchers;

import java.util.Map;
import java.util.Objects;

public class NotNullType implements ResultMatcherType {

  @Override
  public String getName() {
    return "notNull";
  }

  @Override
  public ResultMatcher create(Map<String, Object> options) {
    return Objects::nonNull;
  }
}
