package org.dotwebstack.orchestrate.parser.yaml.mixins;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.List;
import org.dotwebstack.orchestrate.model.PathMapping;

public abstract class PropertyMappingMixin {

  @JsonAlias("pathMappings")
  @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
  private List<PathMapping> pathMappings;
}
