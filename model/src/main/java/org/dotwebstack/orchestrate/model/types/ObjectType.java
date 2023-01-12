package org.dotwebstack.orchestrate.model.types;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import lombok.ToString;

@Getter
@ToString(exclude = {"fieldMap"})
public final class ObjectType implements Type {

  @NotBlank
  private final String name;

  @Valid
  @NotEmpty
  private final List<Field> fields;

  private final List<Field> identityFields;

  private final Map<String, Field> fieldMap;

  @Builder(toBuilder = true)
  private ObjectType(String name, @Singular List<Field> fields) {
    this.name = name;
    this.fields = Collections.unmodifiableList(fields);
    fieldMap = fields.stream()
        .collect(Collectors.toUnmodifiableMap(Field::getName, Function.identity()));
    identityFields = fields.stream()
        .filter(Field::isIdentifier)
        .toList();
  }

  public Optional<Field> getField(String name) {
    return Optional.ofNullable(fieldMap.get(name));
  }

  public boolean containsField(String name) {
    return fieldMap.containsKey(name);
  }

  public boolean hasIdentity() {
    return !identityFields.isEmpty();
  }
}
