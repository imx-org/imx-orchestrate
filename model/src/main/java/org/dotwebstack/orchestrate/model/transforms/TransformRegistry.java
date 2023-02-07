package org.dotwebstack.orchestrate.model.transforms;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TransformRegistry {

  private final Map<String, Transform> transforms;

  private TransformRegistry(Map<String, Transform> transforms) {
    this.transforms = transforms;
  }

  public static TransformRegistryBuilder builder() {
    return new TransformRegistryBuilder();
  }

  public static class TransformRegistryBuilder {

    private final Map<String, Transform> transforms = new HashMap<>();

    public TransformRegistryBuilder register(Transform transform) {
      var name = transform.getName();
      if (transforms.containsKey(name)) {
        throw new TransformRegistryException(
            String.format("Transform with name `%s` already registered. Could not register transform: %s", name,
                transform));
      }

      transforms.put(name, transform);
      return this;
    }

    public TransformRegistryBuilder register(List<Transform> transforms) {
      transforms.forEach(this::register);
      return this;
    }

    public TransformRegistry build() {
      return new TransformRegistry(transforms);
    }
  }

  public Transform getTransform(String name) {
    return transforms.get(name);
  }
}
