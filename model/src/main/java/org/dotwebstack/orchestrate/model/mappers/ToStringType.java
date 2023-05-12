package org.dotwebstack.orchestrate.model.mappers;

import java.util.Map;

public class ToStringType implements ResultMapperType {

  @Override
  public String getName() {
    return "toString";
  }

  @Override
  public ResultMapper create(Map<String, Object> options) {
    return String::valueOf;
  }
}