package org.dotwebstack.orchestrate.model.type;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@Builder
@ToString
public class ObjectField {

  @NonNull
  private final String name;

  @NonNull
  private final Type type;

  @NonNull
  @Builder.Default
  private final Cardinality cardinality = Cardinality.OPTIONAL;

  @Getter
  @ToString
  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  public static class Cardinality {

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
}
