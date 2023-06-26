package org.dotwebstack.orchestrate.model.types;

import java.util.Map;

public interface ValueTypeFactory<T extends ValueType> {

  String getTypeName();

  T create(Map<String, Object> options);
}
