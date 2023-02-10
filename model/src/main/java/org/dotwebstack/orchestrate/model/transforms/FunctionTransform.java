package org.dotwebstack.orchestrate.model.transforms;

import java.util.function.UnaryOperator;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Jacksonized
@Builder
public class FunctionTransform implements Transform {

  private final String name;

  private final UnaryOperator<Object> function;

  @Override
  public String getName() {
    return name;
  }

  @Override
  public Object apply(Object object) {
    return function.apply(object);
  }
}
