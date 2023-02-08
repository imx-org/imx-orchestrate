package org.dotwebstack.orchestrate.model;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import lombok.extern.jackson.Jacksonized;
import org.dotwebstack.orchestrate.model.combiners.Combiner;
import org.dotwebstack.orchestrate.model.transforms.Transform;

@Getter
@Jacksonized
@Builder(toBuilder = true)
public class PropertyPathMapping {

  @Singular
  private final List<PropertyPath> paths;

  private final Combiner combiner;

  @Singular
  private final List<Transform> transforms;

  public boolean hasCombiner() {
    return combiner != null;
  }

  public boolean hasTransforms() {
    return !transforms.isEmpty();
  }
}
