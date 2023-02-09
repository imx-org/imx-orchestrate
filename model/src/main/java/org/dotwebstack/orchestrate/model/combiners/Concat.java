package org.dotwebstack.orchestrate.model.combiners;

import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Jacksonized
@Builder
public final class Concat implements Combiner {

  private static final String DEFAULT_NAME= "concat";

  // NO-OP
  private final String name;

  private final String prefix;

  private final String suffix;

  @SuppressWarnings("java:S4275")
  @Override
  public String getName() {
    return DEFAULT_NAME;
  }

  @Override
  public Object apply(Object value, Object previousValue) {
    if (value == null) {
      return previousValue;
    }

    var appendValue = value.toString();

    if (prefix != null) {
      appendValue = prefix.concat(appendValue);
    }

    if (suffix != null) {
      appendValue = appendValue.concat(suffix);
    }

    return ((String) previousValue).concat(appendValue);
  }
}
