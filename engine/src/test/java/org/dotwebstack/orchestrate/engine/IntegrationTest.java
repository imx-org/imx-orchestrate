package org.dotwebstack.orchestrate.engine;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.dotwebstack.orchestrate.engine.TestFixtures.createModelMapping;

import graphql.schema.idl.SchemaPrinter;
import java.util.Map;
import org.dotwebstack.orchestrate.engine.schema.SchemaFactory;
import org.junit.jupiter.api.Test;
import org.springframework.graphql.execution.DefaultExecutionGraphQlService;
import org.springframework.graphql.execution.GraphQlSource;
import org.springframework.graphql.support.DefaultExecutionGraphQlRequest;
import reactor.test.StepVerifier;

class IntegrationTest {

  @Test
  void create() {
    var schema = new SchemaFactory().create(createModelMapping());
    System.out.println(new SchemaPrinter().print(schema));

    var server = new DefaultExecutionGraphQlService(GraphQlSource.builder(schema)
        .build());

    var request = new DefaultExecutionGraphQlRequest("""
        query {
          area {
            code
            manager
          }
        }
        """, null, Map.of(), Map.of(), randomUUID().toString(), null);

    StepVerifier.create(server.execute(request))
        .assertNext(response -> {
          var executionResult = response.getExecutionResult();
          assertThat(executionResult).isNotNull();
          assertThat(executionResult.getErrors()).isEmpty();
          assertThat(executionResult.isDataPresent()).isTrue();
          System.out.println(executionResult);
        })
        .verifyComplete();
  }
}
