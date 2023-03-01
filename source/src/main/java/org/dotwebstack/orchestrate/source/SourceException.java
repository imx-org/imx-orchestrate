package org.dotwebstack.orchestrate.source;

public class SourceException extends RuntimeException {

  public SourceException(String message) {
    super(message);
  }

  public SourceException(String message, Throwable cause) {
    super(message, cause);
  }
}
