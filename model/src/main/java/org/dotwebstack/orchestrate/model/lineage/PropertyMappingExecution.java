package org.dotwebstack.orchestrate.model.lineage;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(toBuilder = true)
public class PropertyMappingExecution {

  private final PropertyMapping used;
}
