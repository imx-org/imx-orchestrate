package org.dotwebstack.orchestrate.model;

import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;
import org.dotwebstack.orchestrate.model.filters.FilterOperator;

@Getter
@Jacksonized
@Builder(toBuilder = true)
public final class RelationFilterMapping {

  private final String property;

  private final Map<String, Path> keyMapping;

  private final FilterOperator operator;

  private final Path sourcePath;
}
