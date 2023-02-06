package org.dotwebstack.orchestrate.parser.yaml.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.List;
import lombok.extern.jackson.Jacksonized;
import org.dotwebstack.orchestrate.model.FieldPath;

@Jacksonized
public abstract class FieldMappingMixin {

  @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
  private List<FieldPath> sourcePaths;
}
