package nl.geostandaarden.imx.orchestrate.model;

import static java.util.Collections.emptySet;

import java.util.Set;
import nl.geostandaarden.imx.orchestrate.model.types.ValueTypeFactory;

public interface OrchestrateExtension {

  default void registerComponents(ComponentRegistry componentRegistry) {
  }

  default Set<ValueTypeFactory<?>> getValueTypeFactories() {
    return emptySet();
  }
}
