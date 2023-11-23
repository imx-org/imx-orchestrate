package nl.geostandaarden.imx.orchestrate.gateway.schema;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import nl.geostandaarden.imx.orchestrate.engine.exchange.CollectionResult;
import nl.geostandaarden.imx.orchestrate.engine.exchange.ObjectResult;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public final class SchemaVisitor extends GraphQLTypeVisitorStub {

  private final String hasLineageFieldName;

  @Override
  public TraversalControl visitGraphQLFieldDefinition(GraphQLFieldDefinition fieldDefinition, TraverserContext<GraphQLSchemaElement> context) {
    var codeRegistryBuilder = context.getVarFromParents(GraphQLCodeRegistry.Builder.class);

    if (context.getParentNode() instanceof GraphQLObjectType objectType) {
      var currentDataFetcher = codeRegistryBuilder.getDataFetcher(objectType, fieldDefinition);
      codeRegistryBuilder.dataFetcher(objectType, fieldDefinition, wrapDataFetcher(currentDataFetcher, this::mapResult));
    }

    return CONTINUE;
  }

  private Object mapResult(DataFetchingEnvironment environment, Object result) {
    if (result instanceof Mono<?> resultMono) {
      return resultMono.map(this::mapResult)
          .toFuture();
    }

    if (result instanceof List<?> resultList) {
      return resultList.stream()
          .map(this::mapResult)
          .toList();
    }

    if (result instanceof ObjectResult objectResult) {
      return objectResultToMap(objectResult);
    }

    return result;
  }

  private Object mapResult(Object result) {
    if (result instanceof ObjectResult objectResult) {
      return objectResultToMap(objectResult);
    }

    if (result instanceof CollectionResult collectionResult) {
      return collectionResult.getObjectResults()
          .stream()
          .map(this::objectResultToMap)
          .toList();
    }

    return result;
  }

  private Map<String, Object> objectResultToMap(ObjectResult result) {
    var properties = new HashMap<>(result.getProperties());
    properties.put(hasLineageFieldName, result.getLineage());
    return properties;
  }
}
