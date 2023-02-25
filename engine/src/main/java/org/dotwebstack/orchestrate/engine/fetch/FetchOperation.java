package org.dotwebstack.orchestrate.engine.fetch;

import java.util.Map;
import reactor.core.publisher.Flux;

interface FetchOperation {

  Flux<ObjectResult> execute(Map<String, Object> input);
}
