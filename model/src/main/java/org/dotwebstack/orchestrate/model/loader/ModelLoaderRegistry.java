package org.dotwebstack.orchestrate.model.loader;

import java.util.HashMap;
import java.util.Map;
import lombok.NoArgsConstructor;

@NoArgsConstructor(staticName = "getInstance")
public class ModelLoaderRegistry {

  private final Map<String, ModelLoader> modelLoaders = new HashMap<>();

  public void registerModelLoader(ModelLoader modelLoader) {
    modelLoaders.put(modelLoader.getName(), modelLoader);
  }

  public ModelLoader getModelLoader(String name) {
    return modelLoaders.get(name);
  }
}
