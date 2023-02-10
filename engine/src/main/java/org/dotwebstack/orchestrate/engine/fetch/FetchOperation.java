package org.dotwebstack.orchestrate.engine.fetch;

import java.util.Map;
import org.reactivestreams.Publisher;

interface FetchOperation {

  Publisher<ObjectResult> execute(Map<String, Object> input);
}
