package org.dotwebstack.orchestrate.engine.fetch;

import java.util.Map;
import org.reactivestreams.Publisher;

interface FetchOperation {

  Publisher<Map<String, Object>> execute(Map<String, Object> input);
}
