package org.dotwebstack.orchestrate.model.filters;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;
import org.dotwebstack.orchestrate.model.Path;

@Getter
@Jacksonized
@Builder(toBuilder = true)
public final class Filter {

  private final String property;

  private final FilterOperator operator;

  private final Path sourcePath;
}
