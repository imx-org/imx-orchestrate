package org.dotwebstack.orchestrate.parser.yaml;

import java.io.Serial;

public final class YamlModelParserException extends RuntimeException {

  @Serial
  private static final long serialVersionUID = -6223656101641590958L;

  public YamlModelParserException(String message) {
    super(message);
  }

  public YamlModelParserException(String message, Throwable throwable) {
    super(message, throwable);
  }
}
