package org.dotwebstack.orchestrate.model.lineage;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

@Getter
@Builder(toBuilder = true)
public class PropertyPathMapping {

  @Singular("addPath")
  private List<PropertyPath> path;
}

