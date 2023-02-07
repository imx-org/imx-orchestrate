package org.dotwebstack.orchestrate.model.transforms;

import java.util.Collection;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Coalesce implements Transform {

  private static final String NAME = "coalesce";

  private static final Coalesce INSTANCE = new Coalesce();

  public static Coalesce getInstance() {
    return INSTANCE;
  }

  @Override
  public Object apply(Object value) {
    if (!(value instanceof Collection<?>)) {
      throw new TransformException("Coalesce transform can only be applied on lists.");
    }

    return ((Collection<?>) value).stream()
        .filter(Objects::nonNull)
        .findFirst()
        .orElse(null);
  }

  @Override
  public String getName() {
    return NAME;
  }
}
