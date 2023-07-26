package nl.geostandaarden.imx.orchestrate.model.mappers;

public final class ResultMapperException extends RuntimeException {

  public ResultMapperException(String message) {
    super(message);
  }

  public ResultMapperException(String message, Throwable cause) {
    super(message, cause);
  }
}
