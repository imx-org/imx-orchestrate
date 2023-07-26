package nl.geostandaarden.imx.orchestrate.parser.yaml.mixins;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.List;
import nl.geostandaarden.imx.orchestrate.model.PathMapping;
import nl.geostandaarden.imx.orchestrate.model.mappers.ResultMapper;

public abstract class PathMappingMixin {

  @JsonAlias("andThen")
  @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
  private List<PathMapping> nextPathMappings;

  @JsonAlias("map")
  @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
  private List<ResultMapper> resultMappers;
}
