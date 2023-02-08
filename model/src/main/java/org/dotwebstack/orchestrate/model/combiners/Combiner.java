package org.dotwebstack.orchestrate.model.combiners;

public interface Combiner {

  String getName();

  Object apply(Object value, Object previousValue);
}
