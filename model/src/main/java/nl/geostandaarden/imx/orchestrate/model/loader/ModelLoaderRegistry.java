package nl.geostandaarden.imx.orchestrate.model.loader;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ModelLoaderRegistry {

  private final Map<String, ModelLoader> modelLoaders = new HashMap<>();

  public ModelLoaderRegistry register(ModelLoader... modelLoaders) {
    Arrays.stream(modelLoaders).forEach(modelLoader ->
        this.modelLoaders.put(modelLoader.getName(), modelLoader));
    return this;
  }

  public ModelLoader getModelLoader(String name) {
    return modelLoaders.get(name);
  }
}
