package org.dotwebstack.orchestrate.model.types;

public interface ScalarType<T> extends FieldType {

  Class<T> getJavaType();
}
