package org.dotwebstack.orchestrate.model.lineage;

import java.util.Set;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(toBuilder = true)
public final class OrchestratedProperty implements PropertyLineage {

  private final ObjectReference subject;

  private final String property;

  private final Object value;

  private final Set<SourceProperty> isDerivedFrom;
}
