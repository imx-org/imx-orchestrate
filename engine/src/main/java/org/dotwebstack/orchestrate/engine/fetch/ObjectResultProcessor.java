package org.dotwebstack.orchestrate.engine.fetch;

import java.util.Map;
import java.util.function.UnaryOperator;
import lombok.Builder;
import lombok.Singular;

@Builder(toBuilder = true)
public final class ObjectResultProcessor {

  @Singular
  private final Map<String, UnaryOperator<Object>> fieldProcessors;

  public Map<String, Object> process(Map<String, Object> result) {
    return result;
  }
}
