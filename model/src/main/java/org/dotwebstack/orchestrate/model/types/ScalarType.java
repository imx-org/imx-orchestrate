package org.dotwebstack.orchestrate.model.types;

public interface ScalarType<T> extends ValueType {

  Class<T> getJavaType();
}
