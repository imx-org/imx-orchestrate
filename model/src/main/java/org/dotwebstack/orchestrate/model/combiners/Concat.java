package org.dotwebstack.orchestrate.model.combiners;

import lombok.Builder;

@Builder
public final class Concat implements Combiner {

  private static final String NAME = "concat";

  private final String prefix;

  private final String suffix;

  @Override
  public String getName() {
    return NAME;
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
