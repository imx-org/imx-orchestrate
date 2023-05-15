package org.dotwebstack.orchestrate.model.matchers;

import java.util.Map;

public interface MatcherType {

  String getName();

  Matcher create(Map<String, Object> options);

  default void validate(Map<String, Object> options) {}
}
