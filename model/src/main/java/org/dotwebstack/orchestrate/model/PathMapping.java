package org.dotwebstack.orchestrate.model;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import lombok.extern.jackson.Jacksonized;
import org.dotwebstack.orchestrate.model.mappers.ResultMapper;
import org.dotwebstack.orchestrate.model.matchers.ResultMatcher;

@Getter
@Jacksonized
@Builder(toBuilder = true)
public class PathMapping {

  private final Path path;

  private final PathRepeat repeat;

  private final ResultMatcher ifMatch;

  @Singular
  private final List<PathMapping> nextPathMappings;

  @Singular
  private final List<ResultMapper> resultMappers;
}
