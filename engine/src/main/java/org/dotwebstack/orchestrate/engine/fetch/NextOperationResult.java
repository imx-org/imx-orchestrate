package org.dotwebstack.orchestrate.engine.fetch;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class NextOperationResult {

  private final String propertyName;

  private final ObjectResult objectResult;
}
