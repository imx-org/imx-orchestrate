package org.dotwebstack.orchestrate.model;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.dotwebstack.orchestrate.model.types.ObjectTypeRef;

@Getter
@ToString
@SuperBuilder(toBuilder = true)
public final class Relation extends AbstractProperty {

  private final ObjectTypeRef target;

  @Builder.Default
  private final Cardinality sourceCardinality = Cardinality.MULTI;
}