package org.dotwebstack.orchestrate.model.loader;

import java.util.Optional;
import org.dotwebstack.orchestrate.model.Model;
import org.dotwebstack.orchestrate.model.types.ValueTypeRegistry;

public interface ModelLoader {

  String getName();

  Optional<Model> load(String alias, String location, ValueTypeRegistry valueTypeRegistry);
}
