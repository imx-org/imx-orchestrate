package org.dotwebstack.orchestrate.engine.fetch;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

@RequiredArgsConstructor
public final class CollectionFetcher implements DataFetcher<Flux<Map<String, Object>>>  {

  private final FetchPlanner fetchPlanner;

  @Override
  public Flux<Map<String, Object>> get(DataFetchingEnvironment environment) {
    return Flux.empty();
  }
}
