package nl.geostandaarden.imx.orchestrate.model.lineage;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import nl.geostandaarden.imx.orchestrate.model.PathMapping;

@Getter
@Builder(toBuilder = true)
public class PathMappingExecution {

    private final PathMapping used;

    private final List<PathExecution> wasInformedBy;
}
