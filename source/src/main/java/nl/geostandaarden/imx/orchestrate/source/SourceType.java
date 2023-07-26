package nl.geostandaarden.imx.orchestrate.source;

import nl.geostandaarden.imx.orchestrate.model.Model;

import java.util.Map;

public interface SourceType {

    String getName();

    Source create(Model model, Map<String, Object> options);
}
