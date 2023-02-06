package org.dotwebstack.orchestrate.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Cardinality {

  public static final int INFINITE = -1;

  public static final Cardinality REQUIRED = Cardinality.of(1, 1);

  public static final Cardinality OPTIONAL = Cardinality.of(0, 1);

  public static final Cardinality MULTI = Cardinality.of(0, INFINITE);

  private final int min;

  private final int max;

  public static Cardinality of(int min, int max) {
    return new Cardinality(min, max);
  }
}
