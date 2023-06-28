package org.dotwebstack.orchestrate.model.loader;

import org.dotwebstack.orchestrate.model.Model;
import org.dotwebstack.orchestrate.model.types.ValueTypeRegistry;

public interface ModelLoader {

  String getName();

  Model load(String location, ValueTypeRegistry valueTypeRegistry);
}
