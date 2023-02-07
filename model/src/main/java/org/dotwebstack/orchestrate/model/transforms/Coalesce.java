package org.dotwebstack.orchestrate.model.transforms;

import java.util.Collection;
import java.util.Objects;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Jacksonized
@Builder
public class Coalesce implements Transform {

  private final String name;

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
    return name;
  }
}
