package org.dotwebstack.orchestrate;

import static org.dotwebstack.orchestrate.TestFixtures.createModelMapping;

import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import java.util.Map;
import org.dotwebstack.orchestrate.engine.Orchestration;
import org.dotwebstack.orchestrate.engine.schema.SchemaFactory;
import org.dotwebstack.orchestrate.source.DataRepository;
import org.dotwebstack.orchestrate.source.Source;
import org.springframework.boot.autoconfigure.graphql.GraphQlProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.execution.DefaultExecutionGraphQlService;
import org.springframework.graphql.execution.GraphQlSource;
import reactor.core.publisher.Mono;

@Configuration
@EnableConfigurationProperties(GraphQlProperties.class)
public class GatewayConfiguration {

  @Bean
  public GraphQlSource graphQlSource() {
    var orchestration = Orchestration.builder()
        .modelMapping(createModelMapping())
        .source("bag", createSourceStub())
        .build();

    var graphQL = GraphQL.newGraphQL(new SchemaFactory().create(orchestration)).build();

    return new GraphQlSource() {
      @Override
      public GraphQL graphQl() {
        return graphQL;
      }

      @Override
      public GraphQLSchema schema() {
        return graphQL.getGraphQLSchema();
      }
    };
  }

  @Bean
  public DefaultExecutionGraphQlService graphQlService(GraphQlSource graphQlSource) {
    return new DefaultExecutionGraphQlService(graphQlSource);
  }

  private Source createSourceStub() {
    return () -> (DataRepository) objectRequest -> {
      var typeName = objectRequest.getObjectType()
          .getName();

      return switch (typeName) {
        case "Nummeraanduiding" ->
            Mono.just(Map.of("identificatie", "0200200000075716", "huisnummer", 701, "postcode", "7334DP", "ligtAan",
                Map.of("identificatie", "0200300022472362")));
        case "OpenbareRuimte" ->
            Mono.just(Map.of("identificatie", "0200300022472362", "naam", "Laan van Westenenk", "ligtIn", Map.of(
                "identificatie", "3560")));
        case "Woonplaats" -> Mono.just(Map.of("identificatie", "3560", "naam", "Apeldoorn"));
        default -> Mono.error(() -> new RuntimeException("Error!"));
      };
    };
  }
}
