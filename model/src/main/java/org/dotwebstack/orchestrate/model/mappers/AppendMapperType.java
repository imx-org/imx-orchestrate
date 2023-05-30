package org.dotwebstack.orchestrate.model.mappers;

import java.util.Map;

public final class AppendMapperType implements ResultMapperType {

  @Override
  public String getName() {
    return "append";
  }

  @Override
  public ResultMapper create(Map<String, Object> options) {
    var suffix = (String) options.get("suffix");

    return (result, property) -> {
      if (result.isNull()) {
        return result;
      }

      var mappedValue = String.valueOf(result.getValue()).concat(suffix);
      return result.withValue(mappedValue);
    };
  }
}
