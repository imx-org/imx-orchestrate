package org.dotwebstack.orchestrate.model;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import lombok.extern.jackson.Jacksonized;
import org.dotwebstack.orchestrate.model.combiners.ResultCombiner;

@Getter
@Jacksonized
@Builder(toBuilder = true)
public final class PropertyMapping {

  @Singular
  private final List<PathMapping> pathMappings;

  private final ResultCombiner combiner;
}
