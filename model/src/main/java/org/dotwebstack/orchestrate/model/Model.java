package org.dotwebstack.orchestrate.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import lombok.ToString;
import org.dotwebstack.orchestrate.model.type.ObjectField;
import org.dotwebstack.orchestrate.model.type.ObjectType;
import org.dotwebstack.orchestrate.model.type.TypeRef;

@Getter
@ToString(exclude = {"objectTypeMap"})
public final class Model {

  @Valid
  @NotEmpty
  private final List<ObjectType> objectTypes;

  private final Map<String, ObjectType> objectTypeMap;

  @Builder(toBuilder = true)
  private Model(@Singular List<ObjectType> objectTypes) {
    this.objectTypes = resolveTypeRefs(objectTypes);
    objectTypeMap = this.objectTypes.stream()
        .collect(Collectors.toUnmodifiableMap(ObjectType::getName, Function.identity()));
  }

  public Optional<ObjectType> getObjectType(String name) {
    return Optional.ofNullable(objectTypeMap.get(name));
  }

  private static List<ObjectType> resolveTypeRefs(List<ObjectType> objectTypes) {
    return objectTypes.stream()
        .map(objectType -> objectType.toBuilder()
            .clearFields()
            .fields(objectType.getFields()
                .stream()
                .map(objectField -> resolveTypeRefs(objectTypes, objectField))
                .toList())
            .build())
        .toList();
  }

  private static ObjectField resolveTypeRefs(List<ObjectType> objectTypes, ObjectField objectField) {
    var type = objectField.getType();

    if (type instanceof TypeRef) {
      var objectType = objectTypes.stream()
          .filter(o -> o.getName()
              .equals(type.getName()))
          .findAny()
          .orElseThrow(() -> new ModelException(String.format("Object type '%s' not found.", type.getName())));

      return objectField.toBuilder()
          .type(objectType)
          .build();
    }

    return objectField;
  }
}
