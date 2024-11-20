package nl.geostandaarden.imx.orchestrate.model.loader;

import nl.geostandaarden.imx.orchestrate.model.Model;
import nl.geostandaarden.imx.orchestrate.model.types.ValueTypeRegistry;

public interface ModelLoader {

    String getName();

    Model load(String location, ValueTypeRegistry valueTypeRegistry);
}
