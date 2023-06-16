package org.dotwebstack.orchestrate.model;

import java.util.Map;
import org.dotwebstack.orchestrate.model.filters.EqualsOperatorType;
import org.dotwebstack.orchestrate.model.filters.FilterDefinition;

public interface AttributeType {

  String getName();

  default Object mapSourceValue(Object sourceValue) {
    return sourceValue;
  }

  default Object mapLineageValue(Object value) {
    return value;
  }

  default FilterDefinition createFilterDefinition(Path path, Object inputValue) {
    return FilterDefinition.builder()
        .path(path)
        .operator(new EqualsOperatorType().create(Map.of()))
        .value(inputValue)
        .build();
  }
}
