package org.dotwebstack.orchestrate.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import java.util.Optional;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

@Getter
@Builder(toBuilder = true)
public final class ObjectTypeMapping {

  @NotNull
  private final SourceTypeRef sourceRoot;

  @Valid
  @NotEmpty
  @Singular
  private final Map<String, FieldMapping> fieldMappings;

  public Optional<FieldMapping> getFieldMapping(String name) {
    return Optional.ofNullable(fieldMappings.get(name));
  }
}
