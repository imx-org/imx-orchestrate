package nl.geostandaarden.imx.orchestrate.model.mappers;

import java.util.Collection;
import java.util.Map;
import nl.geostandaarden.imx.orchestrate.model.ModelException;
import nl.geostandaarden.imx.orchestrate.model.lineage.PathExecution;
import nl.geostandaarden.imx.orchestrate.model.result.PathResult;

public final class IsNotEmptyMapperType implements ResultMapperType {

    @Override
    public String getName() {
        return "isNotEmpty";
    }

    @Override
    public ResultMapper create(Map<String, Object> options) {
        return (result, property) -> {
            if (result.isNull()) {
                // TODO: Where to get path execution from?
                return PathResult.builder() //
                        .pathExecution(PathExecution.builder().build())
                        .value(false)
                        .build();
            }

            if (result instanceof Collection<?> collection) {
                return result.withValue(!collection.isEmpty());
            }

            throw new ModelException("Mapper 'isNotEmpty' requires a collection value.");
        };
    }
}
