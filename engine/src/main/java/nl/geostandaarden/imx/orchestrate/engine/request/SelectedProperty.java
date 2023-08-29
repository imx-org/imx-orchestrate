package nl.geostandaarden.imx.orchestrate.engine.request;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import nl.geostandaarden.imx.orchestrate.model.Property;

@Getter
@Builder(access = AccessLevel.PACKAGE)
public final class SelectedProperty {

  private final Property property;

  private final DataRequest nestedRequest;
}
