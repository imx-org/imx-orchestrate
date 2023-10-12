package nl.geostandaarden.imx.orchestrate.gateway.fetch;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import java.util.Map;
import java.util.function.UnaryOperator;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ObjectKeyFetcher implements DataFetcher<String> {

  private final UnaryOperator<String> fieldRenamer;

  @SuppressWarnings("unchecked")
  @Override
  public String get(DataFetchingEnvironment environment) {
    var sourceMap = (Map<String, Object>) environment.getSource();
    var objectKey = (Map<String, Object>) sourceMap.get(fieldRenamer.apply("objectKey"));

    return objectKey.values()
        .stream()
        .map(String.class::cast)
        .findFirst()
        .orElseThrow();
  }
}
