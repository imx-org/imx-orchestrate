package nl.geostandaarden.imx.orchestrate.model;

import java.util.Map;
import java.util.Optional;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import lombok.extern.jackson.Jacksonized;

@Getter
@Jacksonized
@Builder(toBuilder = true)
public final class ObjectTypeMapping {

    private final ObjectTypeRef sourceRoot;

    @Singular
    private final Map<String, PropertyMapping> propertyMappings;

    public Optional<PropertyMapping> getPropertyMapping(String name) {
        return Optional.ofNullable(propertyMappings.get(name));
    }

    public Optional<PropertyMapping> getPropertyMapping(Property property) {
        return getPropertyMapping(property.getName());
    }
}
