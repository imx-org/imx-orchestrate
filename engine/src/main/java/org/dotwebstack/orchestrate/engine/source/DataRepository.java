package org.dotwebstack.orchestrate.engine.source;

import java.util.Map;
import org.dotwebstack.orchestrate.model.types.ObjectType;
import reactor.core.publisher.Mono;

public interface DataRepository {

  Mono<Map<String, Object>> findOne(ObjectType objectType, Map<String, Object> objectKey);
}
