package org.dotwebstack.orchestrate.model.type;

import lombok.ToString;

@ToString
public class IntegerType implements ScalarType<Integer> {

  @Override
  public Class<Integer> getJavaType() {
    return Integer.class;
  }

  @Override
  public String getName() {
    return Integer.class.getSimpleName();
  }
}
