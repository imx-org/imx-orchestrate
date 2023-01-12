package org.dotwebstack.orchestrate.engine.fetch;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import java.util.Map;
import reactor.core.publisher.Mono;

public class ObjectFetcher implements DataFetcher<Mono<Map<String, Object>>> {

  @Override
  public Mono<Map<String, Object>> get(DataFetchingEnvironment environment) {
    return Mono.just(Map.of("code", 123, "manager", "Peter"));
  }
}
