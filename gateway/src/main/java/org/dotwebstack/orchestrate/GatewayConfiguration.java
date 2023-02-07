package org.dotwebstack.orchestrate;

import static org.dotwebstack.orchestrate.TestFixtures.createModelMapping;

import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import java.util.LinkedHashMap;
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

  @Bean
  public GraphQlSource graphQlSource() {
    var orchestration = Orchestration.builder()
        .modelMapping(createModelMapping())
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
    var num = new LinkedHashMap<String, Map<String, Object>>();
    num.put("0200200000075716", Map.of("identificatie", "0200200000075716", "huisnummer", 701, "postcode", "7334DP",
        "ligtAan", Map.of("identificatie", "0200300022472362")));
    num.put("0200200000075717", Map.of("identificatie", "0200200000075717", "huisnummer", 702, "postcode", "7334DP",
        "ligtAan", Map.of("identificatie", "0200300022472362")));
    num.put("0200200000075718", Map.of("identificatie", "0200200000075718", "huisnummer", 703, "huisnummertoevoeging", "8", "huisletter", "C", "postcode", "7334DP",
        "ligtAan", Map.of("identificatie", "0200300022472362"), "ligtIn", Map.of("identificatie", "2258")));

    var vbo = new LinkedHashMap<String, Map<String, Object>>();
    vbo.put("0200200000075716", Map.of("identificatie", "0200010000130331"));
    vbo.put("0200200000075718", Map.of("identificatie", "0200010000130331"));

    var opr = new LinkedHashMap<String, Map<String, Object>>();
    opr.put("0200300022472362", Map.of("naam", "Laan van Westenenk", "ligtIn", Map.of("identificatie", "3560")));

    var wpl = new LinkedHashMap<String, Map<String, Object>>();
    wpl.put("3560", Map.of("naam", "Apeldoorn"));
    wpl.put("2258", Map.of("naam", "Beekbergen"));

    return () -> new DataRepository() {
      @Override
      public Mono<Map<String, Object>> findOne(ObjectRequest objectRequest) {
        var typeName = objectRequest.getObjectType()
            .getName();
        var objectKey = (String) objectRequest.getObjectKey()
            .get("identificatie");

        return switch (typeName) {
          case "Nummeraanduiding" -> Mono.justOrEmpty(num.get(objectKey));
          case "OpenbareRuimte" -> Mono.justOrEmpty(opr.get(objectKey));
          case "Woonplaats" -> Mono.justOrEmpty(wpl.get(objectKey));
          default -> Mono.error(() -> new RuntimeException("Error!"));
        };
      }

      @Override
      public Flux<Map<String, Object>> find(CollectionRequest collectionRequest) {
        var typeName = collectionRequest.getObjectType()
            .getName();

        return switch (typeName) {
          case "Nummeraanduiding" -> Flux.fromIterable(num.values());
          case "Verblijfsobject" -> Optional.ofNullable(vbo.get((String) collectionRequest.getFilter()
                  .getValue()))
              .map(Flux::just)
              .orElse(Flux.empty());
          default -> Flux.error(() -> new RuntimeException("Error!"));
        };
      }
    };
  }
}
