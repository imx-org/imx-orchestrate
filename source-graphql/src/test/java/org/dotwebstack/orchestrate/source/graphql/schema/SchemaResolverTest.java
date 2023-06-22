package org.dotwebstack.orchestrate.source.graphql.schema;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.introspection.IntrospectionResultToSchema;
import graphql.language.Document;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.TypeDefinitionRegistry;
import org.dotwebstack.orchestrate.source.graphql.executor.Executor;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Disabled("Doesn't work yet")
@ExtendWith(MockitoExtension.class)
class SchemaResolverTest {

  @Mock
  private Executor executor;

  @Mock
  private ExecutionResult executionResult;

  @InjectMocks
  private SchemaResolver schemaResolver;

  @Test
  void getSchema_returnsExpectedResult() {
    when(executionResult.isDataPresent()).thenReturn(false);
    when(executor.execute(any(ExecutionInput.class))).thenReturn(Mono.just(executionResult));

//    try (MockedConstruction<SchemaGenerator> mockedConstruction = Mockito.mockConstruction(SchemaGenerator.class)) {
    var schemaGenerator = mock(SchemaGenerator.class);
    var introspectionResultToSchema = mock(IntrospectionResultToSchema.class);
    var document = mock(Document.class);
    when(introspectionResultToSchema.createSchemaDefinition(any(ExecutionResult.class))).thenReturn(document);
    var graphQLSchema = mock(GraphQLSchema.class);
    var typeDefinitionRegistry = mock(TypeDefinitionRegistry.class);

//    when(executor.execute(executionResult))
    when(schemaGenerator.makeExecutableSchema(any(), any())).thenReturn(graphQLSchema);
    try (MockedConstruction<SchemaGenerator> mockedConstructionSchemaGenerator = Mockito.mockConstructionWithAnswer(SchemaGenerator.class, invocation -> schemaGenerator);
         MockedConstruction<IntrospectionResultToSchema> mockedConstructionIntrospectionResultToSchema = Mockito.mockConstructionWithAnswer(IntrospectionResultToSchema.class, invocation -> introspectionResultToSchema)
      ) {
      var schema = schemaResolver.getSchema();
    }
  }
}