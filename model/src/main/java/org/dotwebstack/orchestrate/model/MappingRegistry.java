package org.dotwebstack.orchestrate.model;

import java.util.Map;
import lombok.Builder;
import lombok.Singular;
import org.dotwebstack.orchestrate.model.combiners.Combiner;
import org.dotwebstack.orchestrate.model.transforms.Transform;

@Builder
public class MappingRegistry {

  @Singular
  private final Map<String, Transform> transforms;

  @Singular
  private final Map<String, Combiner> combiners;

  public Transform getTransform(String name) {
    return transforms.get(name);
  }

  public Combiner getCombiner(String name) {
    return combiners.get(name);
  }
}
