package nl.geostandaarden.imx.orchestrate.engine;

import java.util.Map;
import java.util.Set;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import nl.geostandaarden.imx.orchestrate.engine.exchange.CollectionRequest;
import nl.geostandaarden.imx.orchestrate.engine.exchange.CollectionResult;
import nl.geostandaarden.imx.orchestrate.engine.exchange.ObjectRequest;
import nl.geostandaarden.imx.orchestrate.engine.exchange.ObjectResult;
import nl.geostandaarden.imx.orchestrate.engine.fetch.FetchPlanner;
import nl.geostandaarden.imx.orchestrate.engine.source.Source;
import nl.geostandaarden.imx.orchestrate.model.ModelMapping;
import reactor.core.publisher.Mono;

@Getter
public final class OrchestrateEngine {

  private final ModelMapping modelMapping;

  private final Map<String, Source> sources;

  private final Set<OrchestrateExtension> extensions;

  private final FetchPlanner fetchPlanner;

  @Builder
  public OrchestrateEngine(ModelMapping modelMapping, @Singular Map<String, Source> sources, @Singular Set<OrchestrateExtension> extensions) {
    this.modelMapping = modelMapping;
    this.sources = sources;
    this.extensions = extensions;
    this.fetchPlanner = new FetchPlanner(modelMapping, sources);
  }

  public Mono<ObjectResult> fetch(ObjectRequest request) {
    return fetchPlanner.fetch(request);
  }

  public Mono<CollectionResult> fetch(CollectionRequest request) {
    return fetchPlanner.fetch(request);
  }
}
