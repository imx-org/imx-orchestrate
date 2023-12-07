package nl.geostandaarden.imx.orchestrate.model.filters;

import lombok.Builder;
import lombok.Getter;
import nl.geostandaarden.imx.orchestrate.model.Path;

@Getter
@Builder(toBuilder = true)
public final class FilterExpression {

  private final Path path;

  @Builder.Default
  private final FilterOperator operator = FilterOperator.builder()
      .type("equals")
      .alias("=")
      .build();

  private final Object value;

  @Override
  public String toString() {
    return path.toString()
        .concat(" ")
        .concat(operator.toString())
        .concat(" ")
        .concat(value.toString());
  }
}
