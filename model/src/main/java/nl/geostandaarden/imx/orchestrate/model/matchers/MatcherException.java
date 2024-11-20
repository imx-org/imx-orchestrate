package nl.geostandaarden.imx.orchestrate.model.matchers;

public final class MatcherException extends RuntimeException {

    public MatcherException(String message) {
        super(message);
    }

    public MatcherException(String message, Throwable cause) {
        super(message, cause);
    }
}
