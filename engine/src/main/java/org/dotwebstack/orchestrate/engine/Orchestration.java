package org.dotwebstack.orchestrate.engine;

import java.util.Map;
import java.util.Set;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import org.dotwebstack.orchestrate.model.ModelMapping;
import org.dotwebstack.orchestrate.source.Source;

@Getter
@Builder(toBuilder = true)
public final class Orchestration {

  private final ModelMapping modelMapping;

  @Singular
  private final Map<String, Source> sources;

  @Singular
  private final Set<OrchestrationExtension> extensions;
}
