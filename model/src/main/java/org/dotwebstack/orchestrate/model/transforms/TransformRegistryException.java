package org.dotwebstack.orchestrate.model.transforms;

public final class TransformRegistryException extends RuntimeException {

  public TransformRegistryException(String message) {
    super(message);
  }

  public TransformRegistryException(String message, Throwable cause) {
    super(message, cause);
  }
}
