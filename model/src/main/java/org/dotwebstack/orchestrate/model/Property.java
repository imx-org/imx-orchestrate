package org.dotwebstack.orchestrate.model;

import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder(toBuilder = true)
public abstract class Property {

  private final String name;

  @Builder.Default
  private final boolean identifier = false;

  public boolean isAttribute() {
    return this instanceof Attribute;
  }

  public boolean isRelation() {
    return this instanceof Relation;
  }
}
