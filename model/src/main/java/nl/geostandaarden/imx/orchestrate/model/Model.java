package nl.geostandaarden.imx.orchestrate.model;

import static java.util.stream.Collectors.toMap;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import lombok.ToString;

@Getter
@ToString(exclude = {"objectTypeMap"})
public final class Model {

  private final String alias;

  private final List<ObjectType> objectTypes;

  private final Map<String, ObjectType> objectTypeMap;

  @Builder(toBuilder = true)
  private Model(String alias, @Singular List<ObjectType> objectTypes) {
    this.alias = alias;
    this.objectTypes = objectTypes;
    this.objectTypeMap = objectTypes.stream()
        .collect(toMap(ObjectType::getName, Function.identity()));
  }

  public ObjectType getObjectType(String name) {
    return Optional.ofNullable(objectTypeMap.get(name))
        .orElseThrow(() -> new ModelException("Object type not found: " + name));
  }

  public ObjectType getObjectType(ObjectTypeRef typeRef) {
    return getObjectType(typeRef.getName());
  }

  public Model replaceObjectType(ObjectType newObjectType) {
    var remainingTypes = objectTypeMap.values()
        .stream()
        .filter(objectType -> !newObjectType.getName().equals(objectType.getName()));

    return toBuilder()
        .clearObjectTypes()
        .objectTypes(Stream.concat(remainingTypes, Stream.of(newObjectType))
            .toList())
        .build();
  }
}
