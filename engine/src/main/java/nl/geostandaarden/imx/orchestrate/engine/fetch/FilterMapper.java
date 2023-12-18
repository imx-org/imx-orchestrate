package nl.geostandaarden.imx.orchestrate.engine.fetch;

import java.util.function.Function;
import nl.geostandaarden.imx.orchestrate.model.filters.FilterExpression;

interface FilterMapper extends Function<FetchInput, FilterExpression> {

  static FilterMapper just(FilterExpression filterExpression) {
    return input -> filterExpression;
  }
}
