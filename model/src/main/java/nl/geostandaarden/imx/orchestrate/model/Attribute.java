package nl.geostandaarden.imx.orchestrate.model;

import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import nl.geostandaarden.imx.orchestrate.model.types.ValueType;

@Getter
@Jacksonized
@ToString(callSuper = true)
@SuperBuilder(toBuilder = true)
public final class Attribute extends AbstractProperty {

  private final ValueType type;
}
