package nl.geostandaarden.imx.orchestrate.model.types;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public final class StringType implements ScalarType<String> {

  @Override
  public Class<String> getJavaType() {
    return String.class;
  }

  @Override
  public String getName() {
    return String.class.getSimpleName();
  }
}
