package nl.geostandaarden.imx.orchestrate.model;

import java.util.Map;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ModelUtils {

    public static Map<String, Object> extractKey(ObjectType objectType, Map<String, Object> properties) {
        return objectType.getIdentityProperties().stream()
                .collect(Collectors.toMap(Property::getName, property -> properties.get(property.getName())));
    }

    public static <T> BinaryOperator<T> noopCombiner() {
        return (a, b) -> {
            throw new IllegalStateException("Combiner should never be called.");
        };
    }
}
