package nl.geostandaarden.imx.orchestrate.model.types;

import lombok.ToString;

@ToString
public final class FloatType implements ScalarType<Float> {

  @Override
  public Class<Float> getJavaType() {
    return Float.class;
  }

  @Override
  public String getName() {
    return Float.class.getSimpleName();
  }
}
