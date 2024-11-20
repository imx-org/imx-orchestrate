package nl.geostandaarden.imx.orchestrate.model;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;
import nl.geostandaarden.imx.orchestrate.model.matchers.Matcher;

@Getter
@Jacksonized
@Builder(toBuilder = true)
public final class PathRepeat {

    private final Matcher untilMatch;
}
