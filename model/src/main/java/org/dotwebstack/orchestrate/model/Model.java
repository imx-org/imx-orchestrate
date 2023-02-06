package org.dotwebstack.orchestrate.model;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import lombok.ToString;
import org.dotwebstack.orchestrate.model.types.ObjectTypeRef;

@Getter
@ToString(exclude = {"objectTypeMap"})
public final class Model {

  private final List<ObjectType> objectTypes;

  private final Map<String, ObjectType> objectTypeMap;

  @Builder(toBuilder = true)
  private Model(@Singular List<ObjectType> objectTypes) {
    this.objectTypes = objectTypes;
    objectTypeMap = this.objectTypes.stream()
        .collect(Collectors.toUnmodifiableMap(ObjectType::getName, Function.identity()));
  }

  public ObjectType getObjectType(ObjectTypeRef typeRef) {
    return getObjectType(typeRef.getName());
  }

  public ObjectType getObjectType(String name) {
    return Optional.ofNullable(objectTypeMap.get(name))
        .orElseThrow(() -> new ModelException("Object type not found: " + name));
  }
}
