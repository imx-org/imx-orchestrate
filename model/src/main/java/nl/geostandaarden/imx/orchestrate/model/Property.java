package nl.geostandaarden.imx.orchestrate.model;

public interface Property {

  String getName();

  boolean isIdentifier();

  Cardinality getCardinality();
}
