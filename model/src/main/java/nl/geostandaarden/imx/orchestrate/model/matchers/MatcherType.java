package nl.geostandaarden.imx.orchestrate.model.matchers;

import java.util.Map;

public interface MatcherType {

    String getName();

    Matcher create(Map<String, Object> options);
}
