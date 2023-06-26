package org.dotwebstack.orchestrate.gateway;

import java.io.Serial;

public class GatewayException extends RuntimeException {

  @Serial
  private static final long serialVersionUID = 7144205435421696426L;

  public GatewayException(String message) {
    super(message);
  }

  public GatewayException(String message, Throwable cause) {
    super(message, cause);
  }
}
