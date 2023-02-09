package org.dotwebstack.orchestrate.parser.yaml.config;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.List;
import org.dotwebstack.orchestrate.model.PropertyPath;
import org.dotwebstack.orchestrate.model.transforms.Transform;

public abstract class PropertyPathMappingMixin {

  @JsonAlias("paths")
  @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
  private List<PropertyPath> paths;

  @JsonAlias("transform")
  @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
  private List<Transform> transforms;
}
