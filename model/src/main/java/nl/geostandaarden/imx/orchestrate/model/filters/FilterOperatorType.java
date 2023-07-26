package nl.geostandaarden.imx.orchestrate.model.filters;

import java.util.Map;

public interface FilterOperatorType {

  String getName();

  FilterOperator create(Map<String, Object> options);
}
