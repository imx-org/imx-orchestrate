package org.dotwebstack.orchestrate.parser.yaml;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import org.dotwebstack.orchestrate.model.Cardinality;
import org.dotwebstack.orchestrate.model.ComponentRegistry;
import org.dotwebstack.orchestrate.model.Model;
import org.dotwebstack.orchestrate.model.ModelMapping;
import org.dotwebstack.orchestrate.model.PathMapping;
import org.dotwebstack.orchestrate.model.PropertyMapping;
import org.dotwebstack.orchestrate.model.combiners.ResultCombiner;
import org.dotwebstack.orchestrate.model.filters.FilterOperator;
import org.dotwebstack.orchestrate.model.loader.ModelLoaderRegistry;
import org.dotwebstack.orchestrate.model.mappers.ResultMapper;
import org.dotwebstack.orchestrate.model.matchers.Matcher;
import org.dotwebstack.orchestrate.parser.yaml.deserializers.CardinalityDeserializer;
import org.dotwebstack.orchestrate.parser.yaml.deserializers.FilterOperatorDeserializer;
import org.dotwebstack.orchestrate.parser.yaml.deserializers.MatcherDeserializer;
import org.dotwebstack.orchestrate.parser.yaml.deserializers.ModelLoaderDeserializer;
import org.dotwebstack.orchestrate.parser.yaml.deserializers.ResultCombinerDeserializer;
import org.dotwebstack.orchestrate.parser.yaml.deserializers.ResultMapperDeserializer;
import org.dotwebstack.orchestrate.parser.yaml.mixins.PathMappingMixin;
import org.dotwebstack.orchestrate.parser.yaml.mixins.PropertyMappingMixin;

public final class YamlModelMappingParser {

  private final YAMLMapper yamlMapper;

  public static YamlModelMappingParser getInstance(ComponentRegistry componentRegistry,
      ModelLoaderRegistry modelLoaderRegistry) {

    var module = new SimpleModule()
        .setMixInAnnotation(PropertyMapping.PropertyMappingBuilder.class, PropertyMappingMixin.class)
        .setMixInAnnotation(PathMapping.PathMappingBuilder.class, PathMappingMixin.class)
        .addDeserializer(ResultCombiner.class, new ResultCombinerDeserializer(componentRegistry))
        .addDeserializer(ResultMapper.class, new ResultMapperDeserializer(componentRegistry))
        .addDeserializer(Matcher.class, new MatcherDeserializer(componentRegistry))
        .addDeserializer(FilterOperator.class, new FilterOperatorDeserializer(componentRegistry))
        .addDeserializer(Cardinality.class, new CardinalityDeserializer())
        .addDeserializer(Model.class, new ModelLoaderDeserializer(modelLoaderRegistry));

    return new YamlModelMappingParser(Set.of(module));
  }

  private YamlModelMappingParser(Set<Module> modules) {
    yamlMapper = new YAMLMapper();
    modules.forEach(yamlMapper::registerModule);
  }

  public ModelMapping parse(InputStream inputStream) {
    try {
      return yamlMapper.readValue(inputStream, ModelMapping.class);
    } catch (IOException e) {
      throw new YamlModelMappingParserException(String.format("An error occurred while processing model mapping:%n%s",
          e.getMessage()), e);
    }
  }
}
