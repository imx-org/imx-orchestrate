package nl.geostandaarden.imx.orchestrate.model.types;

import java.util.Map;
import java.util.Set;

public interface ValueTypeFactory<T extends ValueType> {

    String getTypeName();

    T create(Map<String, Object> options);

    default Set<String> getSupportedFilterTypes() {
        return Set.of("equals");
    }
}
