package org.dotwebstack.orchestrate.engine.schema;

import static graphql.util.TraversalControl.CONTINUE;
import static org.dotwebstack.orchestrate.engine.schema.SchemaUtil.queryField;
import static org.dotwebstack.orchestrate.engine.schema.SchemaUtil.toLowerCamelCase;

import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchemaElement;
import graphql.schema.GraphQLTypeVisitorStub;
import graphql.util.TraversalControl;
import graphql.util.TraverserContext;
import lombok.RequiredArgsConstructor;
import org.dotwebstack.orchestrate.engine.fetch.ObjectFetcher;
import org.dotwebstack.orchestrate.model.ModelMapping;

@RequiredArgsConstructor
public final class SchemaVisitor extends GraphQLTypeVisitorStub {

  private final ModelMapping modelMapping;

  @Override
  public TraversalControl visitGraphQLObjectType(GraphQLObjectType node,
      TraverserContext<GraphQLSchemaElement> context) {
    var codeRegistryBuilder = context.getVarFromParents(GraphQLCodeRegistry.Builder.class);
    var objectTypeName = node.getName();

    modelMapping.getObjectTypeMapping(objectTypeName)
        .ifPresent(objectTypeMapping -> {
          var queryField = queryField(toLowerCamelCase(objectTypeName));
          codeRegistryBuilder.dataFetcher(queryField, new ObjectFetcher(modelMapping));
        });

    return CONTINUE;
  }
}
