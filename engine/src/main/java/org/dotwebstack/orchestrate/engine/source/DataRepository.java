package org.dotwebstack.orchestrate.engine.source;

import java.util.Map;
import reactor.core.publisher.Mono;

public interface DataRepository {

  Mono<Map<String, Object>> findOne(ObjectRequest objectRequest);
}
