package org.dotwebstack.orchestrate.engine.fetch;

import reactor.core.publisher.Flux;

interface FetchOperation {

  Flux<ObjectResult> execute(FetchContext context);
}
