package org.dotwebstack.orchestrate.model;

public interface Property {

  String getName();

  boolean isIdentifier();

  Cardinality getCardinality();
}
