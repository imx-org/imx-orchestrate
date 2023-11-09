package nl.geostandaarden.imx.orchestrate.model.lineage;

import lombok.Builder;
import lombok.Getter;
import nl.geostandaarden.imx.orchestrate.model.Path;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Builder(toBuilder = true)
public class PathExecution {

  private Path used;

  private final ObjectReference startNode;

  @Builder.Default
  private final Set<SourceDataElement> references = new LinkedHashSet<>();
}
