package org.dotwebstack.orchestrate.model.loader;

import java.util.Optional;
import org.dotwebstack.orchestrate.model.Model;

public interface ModelLoader {

  String getProfile();
  Optional<Model> loadModel(String alias, String location);
}
