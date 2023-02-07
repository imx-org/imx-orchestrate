package org.dotwebstack.orchestrate.model.transforms;

import java.util.function.Predicate;
import lombok.Builder;

@Builder
public class TestPredicate implements Transform {

  private final String name;

  private final Predicate<Object> predicate;

  @Override
  public Object apply(Object value) {
    return predicate.test(value);
  }

  @Override
  public String getName() {
    return name;
  }
}
