package nl.geostandaarden.imx.orchestrate.engine;

import java.util.Map;
import java.util.Set;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import nl.geostandaarden.imx.orchestrate.model.ModelMapping;
import nl.geostandaarden.imx.orchestrate.engine.source.Source;

@Getter
@Builder(toBuilder = true)
public final class Orchestration {

  private final ModelMapping modelMapping;

  @Singular
  private final Map<String, Source> sources;

  @Singular
  private final Set<OrchestrateExtension> extensions;
}
