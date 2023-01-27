package org.dotwebstack.orchestrate.engine.fetch;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLObjectType;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.dotwebstack.orchestrate.engine.OrchestrateException;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public final class ObjectFetcher implements DataFetcher<Mono<Map<String, Object>>> {

  private final FetchPlanner fetchPlanner;

  @Override
  public Mono<Map<String, Object>> get(DataFetchingEnvironment environment) {
    var graphQLType = environment.getFieldType();

    if (!(graphQLType instanceof GraphQLObjectType)) {
      throw new OrchestrateException("The object fetcher only supports object types, no unions or interfaces (yet).");
    }

    return fetchPlanner.fetch(environment, (GraphQLObjectType) graphQLType);
  }
}
