package org.dotwebstack.orchestrate.model;

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
import org.dotwebstack.orchestrate.model.type.ObjectType;

@Getter
@ToString(exclude = {"objectTypeMap"})
public final class Model {

  private final List<ObjectType> objectTypes;

  private final Map<String, ObjectType> objectTypeMap;

  @Builder
  private Model(@Singular List<ObjectType> objectTypes) {
    this.objectTypes = Collections.unmodifiableList(objectTypes);
    objectTypeMap = objectTypes.stream()
        .collect(Collectors.toUnmodifiableMap(ObjectType::getName, Function.identity()));
  }

  public Optional<ObjectType> getObjectType(String name) {
    return Optional.ofNullable(objectTypeMap.get(name));
  }
}
