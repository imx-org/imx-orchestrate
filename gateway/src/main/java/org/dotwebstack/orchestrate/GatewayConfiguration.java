package org.dotwebstack.orchestrate;

import static org.dotwebstack.orchestrate.TestFixtures.createBagModel;
import static org.dotwebstack.orchestrate.TestFixtures.createBgtModel;
import static org.dotwebstack.orchestrate.TestFixtures.createBrkModel;
import static org.dotwebstack.orchestrate.TestFixtures.createModelMapping;

import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import java.nio.file.Paths;
import java.util.Set;
import org.dotwebstack.orchestrate.engine.Orchestration;
import org.dotwebstack.orchestrate.engine.schema.SchemaFactory;
import org.dotwebstack.orchestrate.ext.spatial.GeometryExtension;
import org.dotwebstack.orchestrate.model.ComponentFactory;
import org.dotwebstack.orchestrate.model.loader.ModelLoader;
import org.dotwebstack.orchestrate.model.loader.ModelLoaderRegistry;
import org.dotwebstack.orchestrate.parser.yaml.YamlModelMappingParser;
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

  private ModelLoaderRegistry modelLoaderRegistry;

  // TODO temporary to detect availability of modelloaders;
  private Set<ModelLoader> modelLoaders;

  public GatewayConfiguration(GatewayProperties gatewayProperties, Set<ModelLoader> modelLoaders) {
    this.gatewayProperties = gatewayProperties;
    this.modelLoaderRegistry = ModelLoaderRegistry.getInstance();
    modelLoaders.forEach(modelLoaderRegistry::registerModelLoader);
    this.modelLoaders = modelLoaders;
  }

  @Bean
  public GraphQlSource graphQlSource() {
    var extensions = Set.of(new GeometryExtension());

    var componentFactory = new ComponentFactory();
    extensions.forEach(extension -> extension.registerComponents(componentFactory));

    var yamlModelMappingParser = YamlModelMappingParser.getInstance(componentFactory, modelLoaderRegistry);

    var orchestration = Orchestration.builder()
        .modelMapping(createModelMapping(gatewayProperties.getTargetModel(),
            GatewayConfiguration.class.getResourceAsStream(gatewayProperties.getMapping()), yamlModelMappingParser,
            modelLoaders.isEmpty()))

        .source("bag", new FileSource(createBagModel(), Paths.get(gatewayProperties.getDataPath(), "bag")))
        .source("bgt", new FileSource(createBgtModel(), Paths.get(gatewayProperties.getDataPath(), "bgt")))
        .source("brk", new FileSource(createBrkModel(), Paths.get(gatewayProperties.getDataPath(), "brk")))
        .extensions(extensions)
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
}
