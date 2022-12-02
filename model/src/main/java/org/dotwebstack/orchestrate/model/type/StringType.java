package org.dotwebstack.orchestrate.model.type;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class StringType implements ScalarType<String> {

  private static final String NAME = "String";

  @Override
  public Class<String> getJavaType() {
    return String.class;
  }

  @Override
  public String getName() {
    return NAME;
  }
}
