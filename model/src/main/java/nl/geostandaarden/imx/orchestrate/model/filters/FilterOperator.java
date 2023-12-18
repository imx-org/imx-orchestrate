package nl.geostandaarden.imx.orchestrate.model.filters;

import java.util.Map;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FilterOperator {

  private final String type;

  private final String alias;

  private Map<String, Object> options;

  @Override
  public String toString() {
    return alias != null ? alias : type;
  }
}
