package org.dotwebstack.orchestrate.parser.yaml.config;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.List;
import lombok.extern.jackson.Jacksonized;
import org.dotwebstack.orchestrate.model.PropertyPathMapping;

@Jacksonized
public abstract class PropertyMappingMixin {

  @JsonAlias("pathMapping")
  @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
  private List<PropertyPathMapping> pathMappings;
}
