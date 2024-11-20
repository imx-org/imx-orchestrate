package nl.geostandaarden.imx.orchestrate.model.result;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import nl.geostandaarden.imx.orchestrate.model.lineage.PathMappingExecution;

@Getter
@Builder(toBuilder = true)
public class PathMappingResult {

    private final List<PathResult> pathResults;

    private final PathMappingExecution pathMappingExecution;
}
