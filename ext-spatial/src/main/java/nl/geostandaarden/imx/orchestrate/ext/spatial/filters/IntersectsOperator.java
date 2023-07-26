package nl.geostandaarden.imx.orchestrate.ext.spatial.filters;

import lombok.Builder;
import lombok.Getter;
import nl.geostandaarden.imx.orchestrate.model.filters.FilterOperator;

@Getter
@Builder(toBuilder = true)
public class IntersectsOperator implements FilterOperator {

  private final String type;

  @Override
  public String toString() {
    return type;
  }
}
