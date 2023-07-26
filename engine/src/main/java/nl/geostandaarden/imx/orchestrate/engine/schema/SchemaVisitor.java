package nl.geostandaarden.imx.orchestrate.engine.schema;

import static graphql.schema.DataFetcherFactories.wrapDataFetcher;
import static graphql.util.TraversalControl.CONTINUE;

import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchemaElement;
import graphql.schema.GraphQLTypeVisitorStub;
import graphql.util.TraversalControl;
import graphql.util.TraverserContext;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public final class SchemaVisitor extends GraphQLTypeVisitorStub {

  @Override
  public TraversalControl visitGraphQLFieldDefinition(GraphQLFieldDefinition fieldDefinition,
      TraverserContext<GraphQLSchemaElement> context) {
    var codeRegistryBuilder = context.getVarFromParents(GraphQLCodeRegistry.Builder.class);

    if (context.getParentNode() instanceof GraphQLObjectType objectType) {
      var currentDataFetcher = codeRegistryBuilder.getDataFetcher(objectType, fieldDefinition);
      codeRegistryBuilder.dataFetcher(objectType, fieldDefinition, wrapDataFetcher(currentDataFetcher, this::mapResult));
    }

    return CONTINUE;
  }

  private Object mapResult(DataFetchingEnvironment environment, Object result) {
    if (result instanceof Mono<?> resultMono) {
      return resultMono.toFuture();
    }

    if (result instanceof Flux<?> resultFlux) {
      return mapResult(environment, resultFlux.collectList());
    }

    return result;
  }
}
