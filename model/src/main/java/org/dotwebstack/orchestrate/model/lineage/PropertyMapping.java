package org.dotwebstack.orchestrate.model.lineage;

import java.util.Set;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(toBuilder = true)
public class PropertyMapping {

  private final Set<PathMapping> pathMapping;
}
