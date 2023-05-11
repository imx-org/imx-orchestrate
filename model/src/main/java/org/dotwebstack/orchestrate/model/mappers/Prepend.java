package org.dotwebstack.orchestrate.model.mappers;

import java.util.Map;

public class Prepend implements ResultMapperType {

  @Override
  public String getName() {
    return "prepend";
  }

  @Override
  public ResultMapper create(Map<String, Object> options) {
    var prefix = (String) options.get("prefix");

    return result -> {
      if (result == null) {
        return null;
      }

      return prefix.concat(String.valueOf(result));
    };
  }
}
