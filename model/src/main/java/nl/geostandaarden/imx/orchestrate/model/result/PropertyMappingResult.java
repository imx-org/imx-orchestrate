package nl.geostandaarden.imx.orchestrate.model.result;

import java.util.List;
import java.util.Set;
import lombok.Builder;
import lombok.Getter;
import nl.geostandaarden.imx.orchestrate.model.lineage.PropertyMappingExecution;
import nl.geostandaarden.imx.orchestrate.model.lineage.SourceDataElement;

@Getter
@Builder(toBuilder = true)
public final class PropertyMappingResult {

    private final Object value;

    private final Set<SourceDataElement> sourceDataElements;

    private final PropertyMappingExecution propertyMappingExecution;

    private final List<PathMappingResult> pathMappingResults;

    public boolean isEmpty() {
        return value == null;
    }

    public PropertyMappingResult withPropertyMappingExecution(PropertyMappingExecution propertyMappingExecution) {
        return toBuilder().propertyMappingExecution(propertyMappingExecution).build();
    }

    public static PropertyMappingResult empty() {
        return PropertyMappingResult.builder().build();
    }
}
