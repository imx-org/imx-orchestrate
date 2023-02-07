package org.dotwebstack.orchestrate.model.transforms;

public interface TransformRegistrar {

  void registerTransform(TransformRegistry.TransformRegistryBuilder transformRegistryBuilder);
}
