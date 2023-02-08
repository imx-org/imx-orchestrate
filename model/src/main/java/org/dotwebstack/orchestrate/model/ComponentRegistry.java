package org.dotwebstack.orchestrate.model;

import java.util.HashMap;
import java.util.Map;
import org.dotwebstack.orchestrate.model.combiners.Combiner;
import org.dotwebstack.orchestrate.model.transforms.Transform;

public final class ComponentRegistry {

  private final Map<String, Transform> transforms = new HashMap<>();

  private final Map<String, Combiner> combiners = new HashMap<>();

  public ComponentRegistry registerTransform(Transform transform) {
    transforms.put(transform.getName(), transform);
    return this;
  }

  public Transform getTransform(String name) {
    return transforms.get(name);
  }

  public ComponentRegistry registerCombiner(Combiner combiner) {
    combiners.put(combiner.getName(), combiner);
    return this;
  }

  public Combiner getCombiner(String name) {
    return combiners.get(name);
  }
}
