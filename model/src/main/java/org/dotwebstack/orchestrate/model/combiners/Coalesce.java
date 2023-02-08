package org.dotwebstack.orchestrate.model.combiners;

import java.util.Optional;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Coalesce implements Combiner {

  private static final String NAME = "concat";

  private static final Coalesce INSTANCE = new Coalesce();

  public static Coalesce getInstance() {
    return INSTANCE;
  }

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public Object apply(Object value, Object previousValue) {
    return Optional.ofNullable(previousValue)
        .orElse(value);
  }
}
