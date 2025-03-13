package nl.geostandaarden.imx.orchestrate.model;

import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Jacksonized
@Builder(toBuilder = true)
public class ConditionalThen {

    private final List<ConditionalMapping> mapping;

    private final Map<String, Object> values;
}
