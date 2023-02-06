package org.dotwebstack.orchestrate.model.types;

import org.dotwebstack.orchestrate.model.AttributeType;

public interface ScalarType<T> extends AttributeType {

  Class<T> getJavaType();
}
