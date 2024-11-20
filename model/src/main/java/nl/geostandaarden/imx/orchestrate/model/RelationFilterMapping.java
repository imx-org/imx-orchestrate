package nl.geostandaarden.imx.orchestrate.model;

import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Jacksonized
@Builder(toBuilder = true)
public final class RelationFilterMapping {

    private final String property;

    private final Map<String, Path> keyMapping;

    private final String operator;

    private final Path sourcePath;
}
