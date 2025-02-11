package nl.geostandaarden.imx.orchestrate.model.mappers;

import java.util.Map;

public final class DivideMapperType implements ResultMapperType {

    @Override
    public String getName() {
        return "divide";
    }

    @Override
    public ResultMapper create(Map<String, Object> options) {
        var factor = (Integer) options.get("factor");

        return (result, property) -> {
            if (result.isNull()) {
                return result;
            }

            var mappedValue = ((Integer) result.getValue()) / factor;
            return result.withValue(mappedValue);
        };
    }
}
