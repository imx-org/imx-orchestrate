package org.dotwebstack.orchestrate.model.type;

import java.math.BigInteger;
import lombok.ToString;

@ToString
public class IntegerType implements ScalarType<BigInteger> {

  private static final String NAME = "Integer";

  @Override
  public Class<BigInteger> getJavaType() {
    return BigInteger.class;
  }

  @Override
  public String getName() {
    return NAME;
  }
}
