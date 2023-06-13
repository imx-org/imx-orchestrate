package org.dotwebstack.orchestrate.parser.yaml;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import org.dotwebstack.orchestrate.model.ComponentFactory;
import org.dotwebstack.orchestrate.model.Model;
import org.dotwebstack.orchestrate.model.ModelMapping;
import org.dotwebstack.orchestrate.model.PathMapping;
import org.dotwebstack.orchestrate.model.PropertyMapping;
import org.dotwebstack.orchestrate.model.combiners.ResultCombiner;
import org.dotwebstack.orchestrate.model.loader.ModelLoaderRegistry;
import org.dotwebstack.orchestrate.model.mappers.ResultMapper;
import org.dotwebstack.orchestrate.model.matchers.Matcher;
import org.dotwebstack.orchestrate.parser.yaml.deserializers.MatcherDeserializer;
import org.dotwebstack.orchestrate.parser.yaml.deserializers.ModelDeserializer;
import org.dotwebstack.orchestrate.parser.yaml.deserializers.ResultCombinerDeserializer;
import org.dotwebstack.orchestrate.parser.yaml.deserializers.ResultMapperDeserializer;
import org.dotwebstack.orchestrate.parser.yaml.mixins.PathMappingMixin;
import org.dotwebstack.orchestrate.parser.yaml.mixins.PropertyMappingMixin;

public class YamlModelMappingParser {

  private final ObjectMapper yamlObjectMapper;

  public static YamlModelMappingParser getInstance(ModelLoaderRegistry modelLoaderRegistry) {
    var componentFactory = new ComponentFactory();

    var module = new SimpleModule()
        .setMixInAnnotation(PropertyMapping.PropertyMappingBuilder.class, PropertyMappingMixin.class)
        .setMixInAnnotation(PathMapping.PathMappingBuilder.class, PathMappingMixin.class)
        .addDeserializer(ResultCombiner.class, new ResultCombinerDeserializer(componentFactory))
        .addDeserializer(ResultMapper.class, new ResultMapperDeserializer(componentFactory))
        .addDeserializer(Matcher.class, new MatcherDeserializer(componentFactory))
        .addDeserializer(Model.class, new ModelDeserializer(modelLoaderRegistry));

    return new YamlModelMappingParser(new YAMLFactory(), Set.of(module));
  }

  private YamlModelMappingParser(YAMLFactory yamlFactory, Set<Module> modules) {
    yamlObjectMapper = new ObjectMapper(yamlFactory);
    modules.forEach(yamlObjectMapper::registerModule);
  }

  public ModelMapping parse(InputStream inputStream) {
    try {
      return yamlObjectMapper.readValue(inputStream, new TypeReference<>() {});
    } catch (IOException e) {
      throw new YamlModelMappingParserException(String.format("An error occurred while processing model mapping:%n%s",
          e.getMessage()), e);
    }
  }
}
