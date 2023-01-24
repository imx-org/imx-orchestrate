package org.dotwebstack.orchestrate.engine.schema;

import static graphql.schema.DataFetcherFactories.wrapDataFetcher;
import static graphql.util.TraversalControl.CONTINUE;
import static org.apache.commons.lang3.StringUtils.uncapitalize;
import static org.dotwebstack.orchestrate.engine.schema.SchemaUtil.queryField;

import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchemaElement;
import graphql.schema.GraphQLTypeVisitorStub;
import graphql.util.TraversalControl;
import graphql.util.TraverserContext;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.dotwebstack.orchestrate.engine.fetch.ObjectFetcher;
import org.dotwebstack.orchestrate.model.ModelMapping;
import org.dotwebstack.orchestrate.source.Source;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public final class SchemaVisitor extends GraphQLTypeVisitorStub {

  private final ModelMapping modelMapping;

  private final Map<String, Source> sourceMap;

  @Override
  public TraversalControl visitGraphQLObjectType(GraphQLObjectType node,
      TraverserContext<GraphQLSchemaElement> context) {
    var codeRegistryBuilder = context.getVarFromParents(GraphQLCodeRegistry.Builder.class);
    var objectTypeName = node.getName();

    modelMapping.getObjectTypeMapping(objectTypeName)
        .ifPresent(objectTypeMapping -> {
          var queryField = queryField(uncapitalize(objectTypeName));
          var objectFetcher = new ObjectFetcher(modelMapping, sourceMap);
          codeRegistryBuilder.dataFetcher(queryField, wrapDataFetcher(objectFetcher, this::mapResult));
        });

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
