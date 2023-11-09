package nl.geostandaarden.imx.orchestrate.model.lineage;

import lombok.Builder;
import lombok.Getter;
import nl.geostandaarden.imx.orchestrate.model.PropertyMapping;

import java.util.List;

@Getter
@Builder(toBuilder = true)
public class PropertyMappingExecution {

  private final PropertyMapping used;

  private final List<PathMappingExecution> wasInformedBy;
}
