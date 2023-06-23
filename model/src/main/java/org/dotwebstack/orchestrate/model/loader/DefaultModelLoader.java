package org.dotwebstack.orchestrate.model.loader;

import com.google.auto.service.AutoService;
import java.util.Optional;
import org.dotwebstack.orchestrate.model.Model;

@AutoService(ModelLoader.class)
public final class DefaultModelLoader implements ModelLoader {

  @Override
  public String getName() {
    return "default";
  }

  @Override
  public Optional<Model> loadModel(String alias, String location) {
    return Optional.empty();
  }
}
