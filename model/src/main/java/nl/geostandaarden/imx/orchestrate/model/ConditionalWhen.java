package nl.geostandaarden.imx.orchestrate.model;

import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import lombok.extern.jackson.Jacksonized;
import nl.geostandaarden.imx.orchestrate.model.combiners.ResultCombiner;

@Getter
@Jacksonized
@Builder(toBuilder = true)
public class ConditionalWhen {

    @Singular
    private final List<PathMapping> pathMappings;

    private final ResultCombiner combiner;

    private final Map<String, Object> condition;
}
