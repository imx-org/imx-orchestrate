package org.dotwebstack.orchestrate.model.matchers;

public final class ResultMatcherException extends RuntimeException {

  public ResultMatcherException(String message) {
    super(message);
  }

  public ResultMatcherException(String message, Throwable cause) {
    super(message, cause);
  }
}
