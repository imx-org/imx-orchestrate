package org.dotwebstack.orchestrate.model;

import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

@Getter
@Jacksonized
@ToString(callSuper = true)
@SuperBuilder(toBuilder = true)
public final class Attribute extends AbstractProperty {

  private final AttributeType type;
}
