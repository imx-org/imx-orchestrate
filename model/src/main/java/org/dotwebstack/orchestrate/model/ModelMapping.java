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
public final class ModelMapping {

  @Valid
  @NotNull
  private final Model targetModel;

  @Valid
  @NotEmpty
  @Singular
  private final Map<String, Model> sourceModels;

  @Valid
  @NotEmpty
  @Singular
  private final Map<String, ObjectTypeMapping> objectTypeMappings;
}
