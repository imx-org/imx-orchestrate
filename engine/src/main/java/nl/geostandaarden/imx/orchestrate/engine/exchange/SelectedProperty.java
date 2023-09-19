package nl.geostandaarden.imx.orchestrate.engine.exchange;

import lombok.Builder;
import lombok.Getter;
import nl.geostandaarden.imx.orchestrate.model.Property;

@Getter
@Builder(toBuilder = true)
public final class SelectedProperty {

  private final Property property;

  private final DataRequest nestedRequest;
}
