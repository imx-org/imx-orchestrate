package org.dotwebstack.orchestrate;

import static java.util.stream.Collectors.toUnmodifiableMap;
import static java.util.stream.Collectors.toUnmodifiableSet;
import static org.dotwebstack.orchestrate.TestFixtures.createModelMapping;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.dotwebstack.orchestrate.engine.Orchestration;
import org.dotwebstack.orchestrate.engine.schema.SchemaFactory;
import org.dotwebstack.orchestrate.ext.spatial.GeometryExtension;
import org.dotwebstack.orchestrate.model.ComponentFactory;
import org.dotwebstack.orchestrate.model.Model;
import org.dotwebstack.orchestrate.model.loader.ModelLoader;
import org.dotwebstack.orchestrate.model.loader.ModelLoaderRegistry;
import org.dotwebstack.orchestrate.parser.yaml.YamlModelMappingParser;
import org.dotwebstack.orchestrate.source.Source;
import org.dotwebstack.orchestrate.source.SourceType;
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
    var extensions = Set.of(new GeometryExtension());

    var componentFactory = new ComponentFactory();
    extensions.forEach(extension -> extension.registerComponents(componentFactory));

    var modelLoaderRegistry = ModelLoaderRegistry.getInstance();
    var modelLoaders = resolveModelLoaders();
    modelLoaders.forEach(modelLoaderRegistry::registerModelLoader);

    var yamlModelMappingParser = YamlModelMappingParser.getInstance(componentFactory, modelLoaderRegistry);

    var modelMapping = createModelMapping(gatewayProperties.getTargetModel(),
        GatewayConfiguration.class.getResourceAsStream(gatewayProperties.getMapping()), yamlModelMappingParser,
        modelLoaders.isEmpty());

    var sourceModels = modelMapping.getSourceModels().stream()
        .collect(toUnmodifiableMap(Model::getAlias, Function.identity()));

    var sources = gatewayProperties.getSources()
        .entrySet()
        .stream()
        .collect(Collectors.toMap(Map.Entry::getKey, e -> resolveSource(e.getKey(), e.getValue(), sourceModels)));

    var orchestration = Orchestration.builder()
        .modelMapping(modelMapping)
        .sources(sources)
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

  private Set<ModelLoader> resolveModelLoaders() {
    return ServiceLoader.load(ModelLoader.class)
        .stream()
        .map(ServiceLoader.Provider::get)
        .collect(toUnmodifiableSet());
  }

  private Source resolveSource(String dataset, GatewaySource source, Map<String, Model> sourceModels) {
    if (!sourceModels.containsKey(dataset)) {
      throw new GatewayException(String.format("No model with alias `%s` configured in model mapping.", dataset));
    }

    ServiceLoader<SourceType> loader = ServiceLoader.load(SourceType.class);

    return loader.stream()
        .map(ServiceLoader.Provider::get)
        .filter(e -> e.getName().equals(source.getType()))
        .findFirst()
        .map(s -> s.create(sourceModels.get(dataset), source.getOptions()))
        .orElseThrow(() -> new GatewayException(String.format("Source type '%s' not found.", source.getType())));
  }

  @Bean
  public DefaultExecutionGraphQlService graphQlService(GraphQlSource graphQlSource) {
    return new DefaultExecutionGraphQlService(graphQlSource);
  }
}
