package org.dotwebstack.orchestrate.model;

import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import org.dotwebstack.orchestrate.model.types.ValueType;

@Getter
@Jacksonized
@ToString(callSuper = true)
@SuperBuilder(toBuilder = true)
public final class Attribute extends AbstractProperty {

  private final ValueType type;
}
