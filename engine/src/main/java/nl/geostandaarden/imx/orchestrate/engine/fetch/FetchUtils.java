package nl.geostandaarden.imx.orchestrate.engine.fetch;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import nl.geostandaarden.imx.orchestrate.engine.OrchestrateException;

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
}
