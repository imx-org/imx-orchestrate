package nl.geostandaarden.imx.orchestrate.model.matchers;

import java.util.Map;
import java.util.Objects;

public final class IsNullMatcherType implements MatcherType {

    @Override
    public String getName() {
        return "isNull";
    }

    @Override
    public Matcher create(Map<String, Object> options) {
        return Objects::isNull;
    }
}
