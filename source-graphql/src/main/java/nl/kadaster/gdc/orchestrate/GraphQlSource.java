package nl.kadaster.gdc.orchestrate;

import nl.kadaster.gdc.orchestrate.config.GraphQlOrchestrateConfig;
import nl.kadaster.gdc.orchestrate.repository.GraphQlRepository;
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
