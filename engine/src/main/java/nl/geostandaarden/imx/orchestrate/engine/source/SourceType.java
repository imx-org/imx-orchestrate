package nl.geostandaarden.imx.orchestrate.engine.source;

import java.util.Map;
import nl.geostandaarden.imx.orchestrate.model.Model;

public interface SourceType {

    String getName();

    Source create(Model model, Map<String, Object> options);
}
