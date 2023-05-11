package org.dotwebstack.orchestrate.parser.yaml;

import java.io.Serial;

public class ParserException extends RuntimeException {

  @Serial
  private static final long serialVersionUID = 1388266094581155230L;

  public ParserException(String message) {
    super(message);
  }

  public ParserException(String message, Throwable throwable) {
    super(message, throwable);
  }
}
