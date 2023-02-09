package org.dotwebstack.orchestrate.model.lineage;

import java.util.Set;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(toBuilder = true)
public final class ObjectLineage {

  private final Set<OrchestratedProperty> isComposedOf;
}
