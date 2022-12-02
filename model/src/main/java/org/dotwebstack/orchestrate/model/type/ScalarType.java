package org.dotwebstack.orchestrate.model.type;

public interface ScalarType<T> extends Type {

  Class<T> getJavaType();
}
