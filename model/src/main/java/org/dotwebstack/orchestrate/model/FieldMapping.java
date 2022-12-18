package org.dotwebstack.orchestrate.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

@Getter
@Builder(toBuilder = true)
public final class FieldMapping {

  @Valid
  @NotEmpty
  @Singular
  private final List<FieldPath> sourcePaths;
}
