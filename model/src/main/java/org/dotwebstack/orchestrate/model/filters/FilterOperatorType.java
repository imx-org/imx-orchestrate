package org.dotwebstack.orchestrate.model.filters;

import java.util.Map;

public interface FilterOperatorType {

  String getName();

  FilterOperator create(Map<String, Object> options);
}
