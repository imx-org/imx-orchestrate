package nl.geostandaarden.imx.orchestrate.model;

import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@ToString(callSuper = true)
@SuperBuilder(toBuilder = true)
public final class InverseRelation extends AbstractRelation {

  private final Relation originRelation;

  @Override
  public String getName() {
    return originRelation.getInverseName();
  }

  @Override
  public Multiplicity getMultiplicity() {
    return originRelation.getInverseMultiplicity();
  }
}
