package nl.geostandaarden.imx.orchestrate.ext.spatial;

import java.util.Set;
import nl.geostandaarden.imx.orchestrate.ext.spatial.geometry.GeometryTypeFactory;
import nl.geostandaarden.imx.orchestrate.model.OrchestrateExtension;
import nl.geostandaarden.imx.orchestrate.model.types.ValueTypeFactory;

public final class SpatialExtension implements OrchestrateExtension {

  @Override
  public Set<ValueTypeFactory<?>> getValueTypeFactories() {
    return Set.of(new GeometryTypeFactory());
  }
}
