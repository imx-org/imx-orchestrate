package org.dotwebstack.orchestrate.model;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@ToString
@SuperBuilder(toBuilder = true)
public final class Relation extends AbstractProperty {

  private final ObjectTypeRef target;

  private final String inverseName;

  @Builder.Default
  private final Cardinality inverseCardinality = Cardinality.MULTI;
}
