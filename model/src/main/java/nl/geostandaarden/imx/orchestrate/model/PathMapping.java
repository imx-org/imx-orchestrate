package nl.geostandaarden.imx.orchestrate.model;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import lombok.extern.jackson.Jacksonized;
import nl.geostandaarden.imx.orchestrate.model.mappers.ResultMapper;
import nl.geostandaarden.imx.orchestrate.model.matchers.Matcher;

@Getter
@Jacksonized
@Builder(toBuilder = true)
public class PathMapping {

  private final Path path;

  private final PathRepeat repeat;

  private final Matcher ifMatch;

  @Singular
  private final List<PathMapping> nextPathMappings;

  @Singular
  private final List<ResultMapper> resultMappers;
}
