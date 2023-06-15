package org.dotwebstack.orchestrate;

import static org.dotwebstack.orchestrate.TestFixtures.createBagModel;
import static org.dotwebstack.orchestrate.TestFixtures.createBgtModel;
import static org.dotwebstack.orchestrate.TestFixtures.createBrkModel;
import static org.dotwebstack.orchestrate.TestFixtures.createModelMapping;

import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import java.nio.file.Paths;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

import org.dotwebstack.orchestrate.engine.Orchestration;
import org.dotwebstack.orchestrate.engine.schema.SchemaFactory;
import org.dotwebstack.orchestrate.ext.spatial.GeometryExtension;
import org.dotwebstack.orchestrate.model.Model;
import org.dotwebstack.orchestrate.source.Source;
import org.dotwebstack.orchestrate.source.SourceType;
import org.dotwebstack.orchestrate.source.file.FileSource;
import org.springframework.boot.autoconfigure.graphql.GraphQlProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.execution.DefaultExecutionGraphQlService;
import org.springframework.graphql.execution.GraphQlSource;

@Configuration
@EnableConfigurationProperties(GraphQlProperties.class)
public class GatewayConfiguration {

  private GatewayProperties gatewayProperties;

  public GatewayConfiguration(GatewayProperties gatewayProperties) {
    this.gatewayProperties = gatewayProperties;
  }

  @Bean
  public GraphQlSource graphQlSource() {
    var sources = gatewayProperties.getSources()
            .entrySet()
            .stream()
            .collect(Collectors.toMap(Map.Entry::getKey, e -> resolveSource(e.getKey(), e.getValue())));

    var orchestration = Orchestration.builder()
        .modelMapping(createModelMapping(gatewayProperties.getTargetModel(),
            GatewayConfiguration.class.getResourceAsStream(gatewayProperties.getMapping())))
        .sources(sources)
        .extension(new GeometryExtension())
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

  private Source resolveSource(String dataset, GatewaySource source) {
    ServiceLoader<SourceType> loader = ServiceLoader.load(SourceType.class);

    return loader.stream()
        .map(ServiceLoader.Provider::get)
        .filter(e -> e.getName().equals(source.getType()))
        .findFirst()
        .map(s -> s.create(getModel(dataset), source.getOptions()))
        .orElseThrow(() -> new GatewayException(String.format("Source type '%s' not found.", source.getType())));
  }

  private Model getModel(String dataset) {
    return switch(dataset) {
      case "bag" -> createBagModel();
      case "brk" -> createBrkModel();
      case "bgt" -> createBgtModel();
      default -> null;
    };
  }

  @Bean
  public DefaultExecutionGraphQlService graphQlService(GraphQlSource graphQlSource) {
    return new DefaultExecutionGraphQlService(graphQlSource);
  }
}
