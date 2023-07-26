package nl.geostandaarden.imx.orchestrate.model.lineage;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(toBuilder = true)
public final class SourceProperty implements PropertyLineage {

  private final ObjectReference subject;

  private final String property;

  private final List<String> path;

  private final Object value;
}
