package nl.geostandaarden.imx.orchestrate.model.mappers;

import java.util.Map;

public final class PrependMapperType implements ResultMapperType {

    @Override
    public String getName() {
        return "prepend";
    }

    @Override
    public ResultMapper create(Map<String, Object> options) {
        var prefix = (String) options.get("prefix");

        return (result, property) -> {
            if (result.isNull()) {
                return result;
            }

            var mappedValue = prefix.concat(String.valueOf(result.getValue()));
            return result.withValue(mappedValue);
        };
    }
}
