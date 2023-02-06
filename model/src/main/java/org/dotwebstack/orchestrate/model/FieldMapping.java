package org.dotwebstack.orchestrate.model;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import org.dotwebstack.orchestrate.model.transforms.Transform;

@Getter
@Builder(toBuilder = true)
public final class FieldMapping {

  @Singular
  private final List<FieldPath> sourcePaths;

  @Singular
  private final List<Transform> transforms;
}
