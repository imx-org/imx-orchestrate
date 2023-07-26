package nl.geostandaarden.imx.orchestrate.engine;

public final class OrchestrateException extends RuntimeException {

  public OrchestrateException(String message) {
    super(message);
  }

  public OrchestrateException(String message, Throwable cause) {
    super(message, cause);
  }
}
