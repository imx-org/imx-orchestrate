package nl.geostandaarden.imx.orchestrate.ext.spatial.filters;

import java.util.Map;
import nl.geostandaarden.imx.orchestrate.model.filters.FilterOperator;
import nl.geostandaarden.imx.orchestrate.model.filters.FilterOperatorType;

public class IntersectsOperatorType implements FilterOperatorType {

  @Override
  public String getName() {
    return "intersects";
  }

  @Override
  public FilterOperator create(Map<String, Object> options) {
    return IntersectsOperator.builder()
        .type(getName())
        .build();
  }
}
