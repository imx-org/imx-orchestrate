package nl.geostandaarden.imx.orchestrate.model.mappers;

import java.util.Collection;
import java.util.Map;
import nl.geostandaarden.imx.orchestrate.model.ModelException;

public final class IsNotEmptyMapperType implements ResultMapperType {

    @Override
    public String getName() {
        return "isNotEmpty";
    }

    @Override
    public ResultMapper create(Map<String, Object> options) {
        return (result, property) -> {
            if (result.isNull()) {
                return result;
            }

            if (result instanceof Collection<?> collection) {
                return result.withValue(!collection.isEmpty());
            }

            throw new ModelException("Mapper 'isNotEmpty' requires a collection value.");
        };
    }
}
