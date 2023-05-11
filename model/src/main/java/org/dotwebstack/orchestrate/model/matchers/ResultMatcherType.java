package org.dotwebstack.orchestrate.model.matchers;

import java.util.Map;

public interface ResultMatcherType {

  String getName();

  ResultMatcher create(Map<String, Object> options);

  default void validate(Map<String, Object> options) {}
}
