package org.dotwebstack.orchestrate.model.transforms;

import java.util.Collection;
import java.util.Objects;

public class Coalesce implements Transform {

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
}
