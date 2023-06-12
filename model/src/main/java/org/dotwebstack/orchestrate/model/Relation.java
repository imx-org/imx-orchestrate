package org.dotwebstack.orchestrate.model;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

@Getter
@Jacksonized
@ToString(callSuper = true)
@SuperBuilder(toBuilder = true)
public final class Relation extends AbstractRelation {

  private final String inverseName;

  @Builder.Default
  private final Cardinality inverseCardinality = Cardinality.MULTI;
}
