package org.dotwebstack.orchestrate.model.combiners;

public final class CombinerException extends RuntimeException {

  public CombinerException(String message) {
    super(message);
  }

  public CombinerException(String message, Throwable cause) {
    super(message, cause);
  }
}
