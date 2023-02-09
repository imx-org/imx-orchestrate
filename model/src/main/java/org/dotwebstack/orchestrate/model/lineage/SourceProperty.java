package org.dotwebstack.orchestrate.model.lineage;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(toBuilder = true)
public final class SourceProperty {

  private final String property;

  private final Object value;
}
