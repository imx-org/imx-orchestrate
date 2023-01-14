package org.dotwebstack.orchestrate.engine.source;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(toBuilder = true)
public final class Source {

  private final DataRepository dataRepository;
}
