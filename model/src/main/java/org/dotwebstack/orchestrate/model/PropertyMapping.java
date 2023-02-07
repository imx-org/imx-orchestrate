package org.dotwebstack.orchestrate.model;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import lombok.extern.jackson.Jacksonized;
import org.dotwebstack.orchestrate.model.transforms.Transform;

@Getter
@Jacksonized
@Builder(toBuilder = true)
public final class PropertyMapping {

  @Singular
  private final List<PropertyPath> sourcePaths;

  @Singular
  private final List<Transform> transforms;
}
