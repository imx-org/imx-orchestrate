package nl.geostandaarden.imx.orchestrate.model;

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
@ToString(exclude = {"propertyMap"})
public final class ObjectType {

  private final String name;

  private final List<String> supertypes;

  private final List<Property> properties;

  private final Map<String, Property> propertyMap;

  @Builder(toBuilder = true)
  private ObjectType(String name, @Singular List<String> supertypes, @Singular List<Property> properties) {
    this.name = name;
    this.supertypes = Collections.unmodifiableList(supertypes);
    this.properties = Collections.unmodifiableList(properties);
    propertyMap = properties.stream()
        .collect(Collectors.toUnmodifiableMap(Property::getName, Function.identity()));
  }

  public Property getProperty(String name) {
    return Optional.ofNullable(propertyMap.get(name))
        .orElseThrow(() -> new ModelException("Attribute not found: " + name));
  }

  public boolean hasProperty(String name) {
    return propertyMap.containsKey(name);
  }

  public ObjectType appendProperty(Property property) {
    if (propertyMap.containsKey(property.getName())) {
      throw new ModelException("Property already exists: " + property.getName());
    }

    return toBuilder()
        .property(property)
        .build();
  }
}
