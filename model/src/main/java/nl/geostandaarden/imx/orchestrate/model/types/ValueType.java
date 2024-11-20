package nl.geostandaarden.imx.orchestrate.model.types;

import java.util.Map;
import nl.geostandaarden.imx.orchestrate.model.Path;
import nl.geostandaarden.imx.orchestrate.model.filters.FilterExpression;

public interface ValueType {

    String getName();

    default Object mapSourceValue(Object sourceValue) {
        return sourceValue;
    }

    default Object mapLineageValue(Object value) {
        return value;
    }

    default FilterExpression createFilterExpression(Path path, Map<String, Object> inputValue) {
        return FilterExpression.builder().path(path).value(inputValue).build();
    }
}
