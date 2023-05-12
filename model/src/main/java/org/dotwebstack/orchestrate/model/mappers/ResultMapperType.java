package org.dotwebstack.orchestrate.model.mappers;

import java.util.Map;

public interface ResultMapperType {

  String getName();

  ResultMapper create(Map<String, Object> options);

  default void validate(Map<String, Object> options) {}
}
