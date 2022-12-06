package org.dotwebstack.orchestrate.model.mapping;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

@Getter
@Builder(toBuilder = true)
public class ObjectTypeMapping {

  @NotNull
  private final ObjectTypeRef sourceRoot;

  @Valid
  @NotEmpty
  @Singular
  private final Map<String, FieldMapping> fieldMappings;
}
