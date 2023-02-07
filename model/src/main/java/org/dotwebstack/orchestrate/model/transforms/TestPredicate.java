package org.dotwebstack.orchestrate.model.transforms;

import java.util.function.Predicate;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TestPredicate implements Transform {

  private final Predicate<Object> predicate;

  @Override
  public Object apply(Object value) {
    return predicate.test(value);
  }
}
