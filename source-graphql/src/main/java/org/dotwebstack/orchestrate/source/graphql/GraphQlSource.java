package org.dotwebstack.orchestrate.source.graphql;

import org.dotwebstack.orchestrate.source.graphql.config.GraphQlOrchestrateConfig;
import org.dotwebstack.orchestrate.source.graphql.repository.GraphQlRepository;
import org.dotwebstack.orchestrate.source.DataRepository;
import org.dotwebstack.orchestrate.source.Source;

public class GraphQlSource implements Source {

  private final GraphQlOrchestrateConfig config;

  GraphQlSource(GraphQlOrchestrateConfig config) {
    this.config = config;
  }

  @Override
  public DataRepository getDataRepository() {
    return new GraphQlRepository(config);
  }
}
