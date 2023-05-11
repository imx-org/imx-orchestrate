package org.dotwebstack.orchestrate.model.mappers;

import java.util.Map;

public class Append implements ResultMapperType {

  @Override
  public String getName() {
    return "append";
  }

  @Override
  public ResultMapper create(Map<String, Object> options) {
    var suffix = (String) options.get("suffix");

    return result -> {
      if (result == null) {
        return null;
      }

      return String.valueOf(result).concat(suffix);
    };
  }
}
