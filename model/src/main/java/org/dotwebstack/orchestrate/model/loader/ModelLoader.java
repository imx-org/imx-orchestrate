package org.dotwebstack.orchestrate.model.loader;

import java.util.Optional;
import org.dotwebstack.orchestrate.model.Model;

public interface ModelLoader {

  String getName();

  Optional<Model> loadModel(String alias, String location);
}
