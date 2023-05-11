package org.dotwebstack.orchestrate.model.mappers;

import java.util.Map;
import java.util.Objects;

public class Cel implements ResultMapperType {

  @Override
  public String getName() {
    return "cel";
  }

  @Override
  public ResultMapper create(Map<String, Object> options) {
    // TODO: Implement CEL evaluation
    return Objects::nonNull;
  }
}
