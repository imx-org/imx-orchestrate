package org.dotwebstack.orchestrate.model;

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
  public Cardinality getCardinality() {
    return originRelation.getInverseCardinality();
  }
}
