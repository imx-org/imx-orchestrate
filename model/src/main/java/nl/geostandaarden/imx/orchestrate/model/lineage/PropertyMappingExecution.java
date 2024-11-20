package nl.geostandaarden.imx.orchestrate.model.lineage;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import nl.geostandaarden.imx.orchestrate.model.PropertyMapping;

@Getter
@Builder(toBuilder = true)
public class PropertyMappingExecution {

    private final PropertyMapping used;

    private final List<PathMappingExecution> wasInformedBy;
}
