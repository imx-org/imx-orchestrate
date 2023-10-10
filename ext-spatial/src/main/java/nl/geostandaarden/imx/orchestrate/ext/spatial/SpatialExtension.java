package nl.geostandaarden.imx.orchestrate.ext.spatial;

import nl.geostandaarden.imx.orchestrate.ext.spatial.filters.IntersectsOperatorType;
import nl.geostandaarden.imx.orchestrate.ext.spatial.geometry.GeometryTypeFactory;
import nl.geostandaarden.imx.orchestrate.model.ComponentRegistry;
import nl.geostandaarden.imx.orchestrate.model.OrchestrateExtension;
import nl.geostandaarden.imx.orchestrate.model.types.ValueTypeRegistry;

public final class SpatialExtension implements OrchestrateExtension {

  @Override
  public void registerComponents(ComponentRegistry componentRegistry) {
    componentRegistry.register(new IntersectsOperatorType());
  }

  @Override
  public void registerValueTypes(ValueTypeRegistry valueTypeRegistry) {
    valueTypeRegistry.register(new GeometryTypeFactory());
  }
}
