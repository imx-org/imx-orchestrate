package org.dotwebstack.orchestrate.source.graphql.schema;

import graphql.ExecutionInput;
import graphql.introspection.IntrospectionResultToSchema;
import graphql.language.Document;
import graphql.language.SDLDefinition;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.TypeDefinitionRegistry;
import lombok.RequiredArgsConstructor;
import org.dotwebstack.orchestrate.source.graphql.executor.Executor;
import org.dotwebstack.orchestrate.source.graphql.scalar.CoreScalars;
import reactor.core.publisher.Mono;

import static graphql.introspection.IntrospectionQuery.INTROSPECTION_QUERY;

@RequiredArgsConstructor
public class SchemaResolver {

  private final Executor executor;

  public GraphQLSchema getSchema() {
    return new SchemaGenerator().makeExecutableSchema(introspectSchema().block(), RuntimeWiring.newRuntimeWiring()
      .scalar(CoreScalars.DATE)
      .scalar(CoreScalars.DATETIME)
      .build());
  }

  private Mono<TypeDefinitionRegistry> introspectSchema() {
    return this.executor.execute(ExecutionInput.newExecutionInput()
        .query(INTROSPECTION_QUERY)
        .build())
      .map(e -> new IntrospectionResultToSchema().createSchemaDefinition(e))
      .map(SchemaResolver::buildTypeDefinitionRegistry);
  }

  private static TypeDefinitionRegistry buildTypeDefinitionRegistry(Document document) {
    var typeDefinitionRegistry = new TypeDefinitionRegistry();

    document.getDefinitions()
      .stream()
      .filter(SDLDefinition.class::isInstance)
      .map(SDLDefinition.class::cast)
      .forEach(typeDefinitionRegistry::add);

    return typeDefinitionRegistry;
  }

}
