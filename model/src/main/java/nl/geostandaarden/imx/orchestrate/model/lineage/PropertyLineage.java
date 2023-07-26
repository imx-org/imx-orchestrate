package nl.geostandaarden.imx.orchestrate.model.lineage;

public interface PropertyLineage {

  ObjectReference getSubject();

  String getProperty();

  Object getValue();
}
