package org.dotwebstack.orchestrate.engine.fetch;

import java.util.List;
import reactor.core.publisher.Flux;

interface FetchOperation {

  Flux<ObjectResult> execute(FetchInput input);

  Flux<ObjectResult> executeBatch(List<FetchInput> inputs);
}
