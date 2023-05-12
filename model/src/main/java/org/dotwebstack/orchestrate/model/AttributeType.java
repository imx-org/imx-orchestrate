package org.dotwebstack.orchestrate.model;

public interface AttributeType {

  String getName();

  default Object mapSourceValue(Object sourceValue) {
    return sourceValue;
  }

  default Object mapLineageValue(Object value) {
    return value;
  }
}
