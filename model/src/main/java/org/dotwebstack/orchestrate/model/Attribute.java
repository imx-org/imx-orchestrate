package org.dotwebstack.orchestrate.model;

import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@ToString
@SuperBuilder(toBuilder = true)
public final class Attribute extends AbstractProperty {

  private final AttributeType type;
}
