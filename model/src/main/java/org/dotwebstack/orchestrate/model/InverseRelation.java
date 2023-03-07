package org.dotwebstack.orchestrate.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(toBuilder = true)
public class InverseRelation implements Property {

  private final ObjectTypeRef target;

  private final Relation originRelation;

  @Override
  public String getName() {
    return originRelation.getInverseName();
  }

  @Override
  public boolean isIdentifier() {
    return false;
  }

  @Override
  public Cardinality getCardinality() {
    return originRelation.getInverseCardinality();
  }
}
