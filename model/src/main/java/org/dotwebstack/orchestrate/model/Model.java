package org.dotwebstack.orchestrate.model;

import static java.util.Collections.unmodifiableMap;
import static java.util.stream.Collectors.toMap;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
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
    // Pre-collect object types in map structure
    var mutableObjectTypeMap = objectTypes.stream()
        .collect(toMap(ObjectType::getName, Function.identity()));

    // Discover & add inverse relation properties
    objectTypes.forEach(objectType -> objectType.getProperties(Relation.class)
        .forEach(relation -> Optional.ofNullable(relation.getInverseName())
            .ifPresent(inverseName -> {
              var targetRef = relation.getTarget();

              mutableObjectTypeMap.computeIfPresent(targetRef.getName(), (targetName, targetType) -> targetType.toBuilder()
                  .property(InverseRelation.builder()
                      .target(objectType.getRef())
                      .originRelation(relation)
                      .build())
                  .build());
            })));

    this.objectTypes = List.copyOf(mutableObjectTypeMap.values());
    this.objectTypeMap = unmodifiableMap(mutableObjectTypeMap);
  }

  public ObjectType getObjectType(ObjectTypeRef typeRef) {
    return getObjectType(typeRef.getName());
  }

  public ObjectType getObjectType(String name) {
    return Optional.ofNullable(objectTypeMap.get(name))
        .orElseThrow(() -> new ModelException("Object type not found: " + name));
  }
}
