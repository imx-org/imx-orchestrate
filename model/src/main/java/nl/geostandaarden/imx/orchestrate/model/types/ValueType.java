package nl.geostandaarden.imx.orchestrate.model.types;

import java.util.Map;
import nl.geostandaarden.imx.orchestrate.model.Path;
import nl.geostandaarden.imx.orchestrate.model.filters.EqualsOperatorType;
import nl.geostandaarden.imx.orchestrate.model.filters.FilterDefinition;

public interface ValueType {

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
