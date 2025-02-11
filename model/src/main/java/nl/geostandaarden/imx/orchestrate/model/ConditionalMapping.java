package nl.geostandaarden.imx.orchestrate.model;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Jacksonized
@Builder(toBuilder = true)
public class ConditionalMapping {

    private final List<ConditionalWhen> when;

    private final ConditionalThen then;

    private final ConditionalThen otherwise;
}
