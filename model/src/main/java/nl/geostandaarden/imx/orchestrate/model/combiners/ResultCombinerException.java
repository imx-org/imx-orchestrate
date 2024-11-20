package nl.geostandaarden.imx.orchestrate.model.combiners;

public final class ResultCombinerException extends RuntimeException {

    public ResultCombinerException(String message) {
        super(message);
    }

    public ResultCombinerException(String message, Throwable cause) {
        super(message, cause);
    }
}
