package org.dotwebstack.orchestrate.model.type;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@Builder(toBuilder = true)
public class ObjectField {

  @NotBlank
  private final String name;

  @NotNull
  private final Type type;

  @Builder.Default
  private final boolean identifier = false;

  @NotNull
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
