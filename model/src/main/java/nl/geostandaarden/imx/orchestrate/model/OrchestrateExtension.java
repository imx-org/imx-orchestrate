package nl.geostandaarden.imx.orchestrate.model;

import nl.geostandaarden.imx.orchestrate.model.types.ValueTypeRegistry;

public interface OrchestrateExtension {

  default void registerComponents(ComponentRegistry componentRegistry) {
  }

  default void registerValueTypes(ValueTypeRegistry valueTypeRegistry) {
  }
}
