package nl.geostandaarden.imx.orchestrate.model.lineage;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(toBuilder = true)
public class Path {

  private final ObjectReference startNode;

  private final List<String> segments;

  @Builder.Default
  private final Set<SourceProperty> references = new LinkedHashSet<>();
}