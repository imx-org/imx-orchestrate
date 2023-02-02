package org.dotwebstack.orchestrate.engine.fetch;

import static graphql.schema.GraphQLTypeUtil.unwrapAll;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLObjectType;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.dotwebstack.orchestrate.engine.OrchestrateException;
import org.reactivestreams.Publisher;

@RequiredArgsConstructor
public final class GenericDataFetcher implements DataFetcher<Publisher<Map<String, Object>>> {

  private final FetchPlanner fetchPlanner;

  @Override
  public Publisher<Map<String, Object>> get(DataFetchingEnvironment environment) {
    var graphQLType = unwrapAll(environment.getFieldType());

    if (!(graphQLType instanceof GraphQLObjectType)) {
      throw new OrchestrateException("The object fetcher only supports object types, no unions or interfaces (yet).");
    }

    return fetchPlanner.fetch(environment, (GraphQLObjectType) graphQLType);
  }
}
