package org.dotwebstack.orchestrate.model.type;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ScalarTypes {

  public static final BooleanType BOOLEAN = new BooleanType();

  public static final DoubleType DOUBLE = new DoubleType();

  public static final FloatType FLOAT = new FloatType();

  public static final IntegerType INTEGER = new IntegerType();

  public static final LongType LONG = new LongType();

  public static final StringType STRING = new StringType();
}
