package org.dotwebstack.orchestrate.model.type;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Singular;
import lombok.ToString;

@Getter
@ToString(exclude = {"fieldMap"})
public class ObjectType implements Type {

  @NonNull
  private final String name;

  @Valid
  @NotEmpty
  private final List<ObjectField> fields;

  private final List<ObjectField> identityFields;

  private final Map<String, ObjectField> fieldMap;

  @Builder(toBuilder = true)
  private ObjectType(String name, @Singular List<ObjectField> fields) {
    this.name = name;
    this.fields = Collections.unmodifiableList(fields);
    fieldMap = fields.stream()
        .collect(Collectors.toUnmodifiableMap(ObjectField::getName, Function.identity()));
    identityFields = fields.stream()
        .filter(ObjectField::isIdentifier)
        .toList();
  }

  public Optional<ObjectField> getField(String name) {
    return Optional.ofNullable(fieldMap.get(name));
  }

  public boolean hasIdentity() {
    return !identityFields.isEmpty();
  }
}
