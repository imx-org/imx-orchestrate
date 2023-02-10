package org.dotwebstack.orchestrate.model.lineage;

public interface PropertyLineage {

  ObjectReference getSubject();

  String getProperty();

  Object getValue();
}
