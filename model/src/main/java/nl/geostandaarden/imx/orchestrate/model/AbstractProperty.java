package nl.geostandaarden.imx.orchestrate.model;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@ToString(callSuper = true)
@SuperBuilder(toBuilder = true)
public abstract class AbstractProperty implements Property {

  protected final String name;

  protected final boolean identifier;

  @Builder.Default
  protected final Cardinality cardinality = Cardinality.OPTIONAL;
}
