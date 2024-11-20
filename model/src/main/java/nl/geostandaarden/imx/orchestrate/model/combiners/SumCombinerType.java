package nl.geostandaarden.imx.orchestrate.model.combiners;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import nl.geostandaarden.imx.orchestrate.model.lineage.PathExecution;
import nl.geostandaarden.imx.orchestrate.model.result.PathResult;
import nl.geostandaarden.imx.orchestrate.model.result.PropertyMappingResult;

public final class SumCombinerType implements ResultCombinerType {

    @Override
    public String getName() {
        return "sum";
    }

    @Override
    public ResultCombiner create(Map<String, Object> options) {
        return pathResults -> {
            // TODO: Support double/decimal types? Improve type safety?
            var sumValue = pathResults.stream()
                    .map(PathResult::getValue)
                    .map(Integer.class::cast)
                    .mapToInt(Integer::intValue)
                    .sum();

            return PropertyMappingResult.builder()
                    .value(sumValue)
                    .sourceDataElements(pathResults.stream()
                            .map(PathResult::getPathExecution)
                            .map(PathExecution::getReferences)
                            .flatMap(Set::stream)
                            .collect(Collectors.toSet()))
                    .build();
        };
    }
}
