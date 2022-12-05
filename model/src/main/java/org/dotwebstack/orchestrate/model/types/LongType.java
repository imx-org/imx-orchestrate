package org.dotwebstack.orchestrate.model.types;

import lombok.ToString;

@ToString
public class LongType implements ScalarType<Long> {

  @Override
  public Class<Long> getJavaType() {
    return Long.class;
  }

  @Override
  public String getName() {
    return Long.class.getSimpleName();
  }
}
