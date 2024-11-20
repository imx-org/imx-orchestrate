package nl.geostandaarden.imx.orchestrate.model.mappers;

import java.util.Map;

public interface ResultMapperType {

    String getName();

    ResultMapper create(Map<String, Object> options);
}
