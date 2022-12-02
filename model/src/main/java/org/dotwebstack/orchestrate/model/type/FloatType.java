package org.dotwebstack.orchestrate.model.type;

import lombok.ToString;

@ToString
public class FloatType implements ScalarType<Float> {

  @Override
  public Class<Float> getJavaType() {
    return Float.class;
  }

  @Override
  public String getName() {
    return Float.class.getSimpleName();
  }
}
