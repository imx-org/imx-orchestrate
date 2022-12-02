package org.dotwebstack.orchestrate.model.type;

import lombok.ToString;

@ToString
public class BooleanType implements ScalarType<Boolean> {

  @Override
  public Class<Boolean> getJavaType() {
    return Boolean.class;
  }

  @Override
  public String getName() {
    return Boolean.class.getSimpleName();
  }
}
