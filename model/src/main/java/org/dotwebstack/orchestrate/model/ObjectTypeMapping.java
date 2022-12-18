package org.dotwebstack.orchestrate.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

@Getter
@Builder(toBuilder = true)
public final class ObjectTypeMapping {

  @NotNull
  private final SourceRoot sourceRoot;

  @Valid
  @NotEmpty
  @Singular
  private final Map<String, FieldMapping> fieldMappings;
}
