package org.dotwebstack.orchestrate.engine.source;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class Source {

  @Getter
  private final DataRepository dataRepository;
}
