package org.dotwebstack.orchestrate.model.type;

import lombok.ToString;

@ToString
public class DoubleType implements ScalarType<Double> {

  @Override
  public Class<Double> getJavaType() {
    return Double.class;
  }

  @Override
  public String getName() {
    return Double.class.getSimpleName();
  }
}
