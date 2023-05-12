package org.dotwebstack.orchestrate.model.combiners;

import java.util.Map;

public interface ResultCombinerType {

  String getName();

  ResultCombiner create(Map<String, Object> options);

  default void validate(Map<String, Object> options) {}
}
