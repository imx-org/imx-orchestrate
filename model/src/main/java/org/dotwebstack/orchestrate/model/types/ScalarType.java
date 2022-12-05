package org.dotwebstack.orchestrate.model.types;

public interface ScalarType<T> extends Type {

  Class<T> getJavaType();
}
