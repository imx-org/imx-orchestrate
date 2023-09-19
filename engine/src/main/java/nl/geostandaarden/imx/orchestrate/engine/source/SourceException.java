package nl.geostandaarden.imx.orchestrate.engine.source;

public class SourceException extends RuntimeException {

  public SourceException(String message) {
    super(message);
  }

  public SourceException(String message, Throwable cause) {
    super(message, cause);
  }
}
