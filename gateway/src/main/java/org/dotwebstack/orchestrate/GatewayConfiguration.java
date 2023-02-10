package org.dotwebstack.orchestrate;

import static org.dotwebstack.orchestrate.TestFixtures.NUM_DATA;
import static org.dotwebstack.orchestrate.TestFixtures.OPR_DATA;
import static org.dotwebstack.orchestrate.TestFixtures.VBO_DATA;
import static org.dotwebstack.orchestrate.TestFixtures.WPL_DATA;
import static org.dotwebstack.orchestrate.TestFixtures.createModelMapping;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import java.util.Map;
import java.util.Optional;
import org.dotwebstack.orchestrate.engine.Orchestration;
import org.dotwebstack.orchestrate.engine.schema.SchemaFactory;
import org.dotwebstack.orchestrate.source.CollectionRequest;
import org.dotwebstack.orchestrate.source.DataRepository;
import org.dotwebstack.orchestrate.source.ObjectRequest;
import org.dotwebstack.orchestrate.source.Source;
import org.springframework.boot.autoconfigure.graphql.GraphQlProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.execution.DefaultExecutionGraphQlService;
import org.springframework.graphql.execution.GraphQlSource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Configuration
@EnableConfigurationProperties(GraphQlProperties.class)
public class GatewayConfiguration {

  private GatewayProperties gatewayProperties;

  public GatewayConfiguration(GatewayProperties gatewayProperties) {
    this.gatewayProperties = gatewayProperties;
  }

  @Bean
  public GraphQlSource graphQlSource() {
    var orchestration = Orchestration.builder()
        .modelMapping(createModelMapping(gatewayProperties.getTargetModel(),
            GatewayConfiguration.class.getResourceAsStream(gatewayProperties.getMapping())))
        .source("bag", createSourceStub())
        .build();

    var graphQL = GraphQL.newGraphQL(SchemaFactory.create(orchestration)).build();

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
    return () -> new DataRepository() {
      @Override
      public Mono<Map<String, Object>> findOne(ObjectRequest objectRequest) {
        var typeName = objectRequest.getObjectType()
            .getName();
        var objectKey = (String) objectRequest.getObjectKey()
            .get("identificatie");

        return switch (typeName) {
          case "Nummeraanduiding" -> Mono.justOrEmpty(NUM_DATA.get(objectKey));
          case "OpenbareRuimte" -> Mono.justOrEmpty(OPR_DATA.get(objectKey));
          case "Woonplaats" -> Mono.justOrEmpty(WPL_DATA.get(objectKey));
          default -> Mono.error(() -> new RuntimeException("Error!"));
        };
      }

      @Override
      public Flux<Map<String, Object>> find(CollectionRequest collectionRequest) {
        var typeName = collectionRequest.getObjectType()
            .getName();

        return switch (typeName) {
          case "Nummeraanduiding" -> Flux.fromIterable(NUM_DATA.values());
          case "Verblijfsobject" -> Optional.ofNullable(VBO_DATA.get((String) collectionRequest.getFilter()
                  .getValue()))
              .map(Flux::just)
              .orElse(Flux.empty());
          default -> Flux.error(() -> new RuntimeException("Error!"));
        };
      }
    };
  }
}
