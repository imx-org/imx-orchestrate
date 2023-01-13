package org.dotwebstack.orchestrate.engine.fetch;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.dotwebstack.orchestrate.model.ModelMapping;
import org.dotwebstack.orchestrate.model.ObjectTypeMapping;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class ObjectFetcher implements DataFetcher<Mono<Map<String, Object>>> {

  private final ModelMapping modelMapping;

  private final ObjectTypeMapping objectTypeMapping;

  @Override
  public Mono<Map<String, Object>> get(DataFetchingEnvironment environment) {
    return Mono.just(Map.of("code", 123, "manager", "Peter"));
  }
}
