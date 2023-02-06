package org.dotwebstack.orchestrate.model;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@ToString
@SuperBuilder(toBuilder = true)
public final class Attribute extends Property {

  private final AttributeType type;

  @Builder.Default
  private final Cardinality cardinality = Cardinality.OPTIONAL;
}
