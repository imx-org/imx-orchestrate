package nl.geostandaarden.imx.orchestrate.engine.fetch;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import nl.geostandaarden.imx.orchestrate.engine.OrchestrateException;
import nl.geostandaarden.imx.orchestrate.engine.exchange.ObjectResult;
import nl.geostandaarden.imx.orchestrate.model.Attribute;
import nl.geostandaarden.imx.orchestrate.model.ModelException;
import nl.geostandaarden.imx.orchestrate.model.Path;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FetchUtils {

    @SuppressWarnings("unchecked")
    public static <T> T cast(Object value) {
        return (T) value;
    }

    public static <T> T cast(Object value, Class<T> clazz) {
        if (!clazz.isInstance(value)) {
            throw new OrchestrateException("Could not cast value to class: " + clazz.getSimpleName());
        }

        return clazz.cast(value);
    }

    public static Map<String, Object> keyFromResult(ObjectResult objectResult, Map<String, Path> keyMapping) {
        // TODO: Support various edge cases
        var keysPresent = keyMapping.values().stream()
                .allMatch(key -> objectResult.getProperties().containsKey(key.getFirstSegment()));

        if (!keysPresent) {
            return null;
        }

        return keyMapping.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> {
            var keyPath = entry.getValue();

            if (!keyPath.isLeaf()) {
                throw new ModelException("Only leaf paths are (currently) supported: " + keyPath);
            }

            var objectType = objectResult.getType();
            var propertyName = keyPath.getFirstSegment();

            if (!(objectType.getProperty(propertyName) instanceof Attribute)) {
                throw new ModelException("Only attribute keys are (currently) supported: " + propertyName);
            }

            return Optional.ofNullable(objectResult.getProperty(propertyName))
                    .orElseThrow(() -> new ModelException("Key properties may never be null."));
        }));
    }
}
