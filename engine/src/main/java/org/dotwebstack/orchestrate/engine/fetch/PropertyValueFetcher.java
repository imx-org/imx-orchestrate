package org.dotwebstack.orchestrate.engine.fetch;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import java.util.Map;
import org.dotwebstack.orchestrate.engine.OrchestrateException;
import org.dotwebstack.orchestrate.model.lineage.PropertyLineage;

public final class PropertyValueFetcher implements DataFetcher<Map<String, Object>> {

  @Override
  public Map<String, Object> get(DataFetchingEnvironment environment) {
    var value = ((PropertyLineage) environment.getSource())
        .getValue();

    if (value instanceof Integer) {
      return Map.of("integerValue", value);
    }

    if (value instanceof Boolean) {
      return Map.of("booleanValue", value);
    }

    if (value instanceof String) {
      return Map.of("stringValue", value);
    }

    throw new OrchestrateException("Could not map value: " + value.toString());
  }
}
