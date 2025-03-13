package nl.geostandaarden.imx.orchestrate.engine.exchange;

import static nl.geostandaarden.imx.orchestrate.model.ModelUtils.extractKey;

import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import lombok.ToString;
import nl.geostandaarden.imx.orchestrate.model.ObjectType;
import nl.geostandaarden.imx.orchestrate.model.lineage.ObjectLineage;
import nl.geostandaarden.imx.orchestrate.model.lineage.ObjectReference;

@Getter
@ToString
@Builder(toBuilder = true)
public class ObjectResult implements DataResult {

    private final ObjectType type;

    @Singular
    private final Map<String, Object> properties;

    private final ObjectLineage lineage;

    public Map<String, Object> getKey() {
        return extractKey(type, properties);
    }

    public Object getProperty(String name) {
        return properties.get(name);
    }

    public ObjectResult withProperties(Map<String, Object> properties) {
        return toBuilder().properties(properties).build();
    }

    public ObjectResult replaceProperties(Map<String, Object> properties) {
        return toBuilder() //
                .clearProperties()
                .properties(properties)
                .build();
    }

    public ObjectReference getObjectReference() {
        return ObjectReference.builder()
                .objectType(type.getName())
                .objectKey(extractKey(type, properties))
                .build();
    }
}
