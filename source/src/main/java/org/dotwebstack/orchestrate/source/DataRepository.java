package org.dotwebstack.orchestrate.source;

import java.util.Map;
import reactor.core.publisher.Mono;

public interface DataRepository {

  Mono<Map<String, Object>> findOne(ObjectRequest objectRequest);
}
