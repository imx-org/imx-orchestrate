package org.dotwebstack.orchestrate.engine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.dotwebstack.orchestrate.engine.TestFixtures.createModelMapping;

import graphql.GraphQL;
import graphql.schema.idl.SchemaPrinter;
import org.dotwebstack.orchestrate.engine.schema.SchemaFactory;
import org.junit.jupiter.api.Test;

class IntegrationTest {

  @Test
  void create() {
    var schema = new SchemaFactory().create(createModelMapping());
    System.out.println(new SchemaPrinter().print(schema));

    var graphQL = GraphQL.newGraphQL(schema)
        .build();

    var result = graphQL.execute("""
          query {
            town {
              id
              name
              municipality
              province
            }
          }
        """);

    assertThat(result).isNotNull();
    assertThat(result.getErrors()).isEmpty();
    assertThat(result.isDataPresent()).isTrue();
    System.out.println(result);
  }
}
