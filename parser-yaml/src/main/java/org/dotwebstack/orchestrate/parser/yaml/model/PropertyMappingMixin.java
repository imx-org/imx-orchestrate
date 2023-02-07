package org.dotwebstack.orchestrate.parser.yaml.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.List;
import lombok.extern.jackson.Jacksonized;
import org.dotwebstack.orchestrate.model.PropertyPath;

@Jacksonized
public abstract class PropertyMappingMixin {

  @JsonAlias("sourcePath")
  @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
  private List<PropertyPath> sourcePaths;
}
