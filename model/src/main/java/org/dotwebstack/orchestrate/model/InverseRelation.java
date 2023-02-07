package org.dotwebstack.orchestrate.model;

import lombok.Builder;
import lombok.Getter;
import org.dotwebstack.orchestrate.model.types.ObjectTypeRef;

@Getter
@Builder(toBuilder = true)
public class InverseRelation implements Property {

  private final ObjectTypeRef target;

  private final Relation relation;

  @Override
  public String getName() {
    return relation.getInverseName();
  }

  @Override
  public boolean isIdentifier() {
    return false;
  }

  @Override
  public Cardinality getCardinality() {
    return relation.getInverseCardinality();
  }
}
