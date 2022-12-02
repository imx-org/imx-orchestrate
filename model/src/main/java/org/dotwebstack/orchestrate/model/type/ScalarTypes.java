package org.dotwebstack.orchestrate.model.type;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ScalarTypes {

  public static final IntegerType INTEGER = new IntegerType();

  public static final StringType STRING = new StringType();
}
