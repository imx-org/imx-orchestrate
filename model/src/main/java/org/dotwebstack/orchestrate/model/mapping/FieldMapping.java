package org.dotwebstack.orchestrate.model.mapping;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

@Getter
@Builder(toBuilder = true)
public class FieldMapping {

  @Singular
  private final List<FieldPath> sourcePaths;
}
