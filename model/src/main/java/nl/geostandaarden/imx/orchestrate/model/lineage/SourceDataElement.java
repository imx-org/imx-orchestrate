package nl.geostandaarden.imx.orchestrate.model.lineage;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(toBuilder = true)
public final class SourceDataElement implements DataElement {

  private final ObjectReference subject;

  private final String property;

  private final Object value;
}
