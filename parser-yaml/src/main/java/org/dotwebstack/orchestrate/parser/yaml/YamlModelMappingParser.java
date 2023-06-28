package org.dotwebstack.orchestrate.parser.yaml;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import java.io.IOException;
import java.io.InputStream;
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
import org.dotwebstack.orchestrate.model.types.ValueTypeRegistry;
import org.dotwebstack.orchestrate.parser.yaml.deserializers.CardinalityDeserializer;
import org.dotwebstack.orchestrate.parser.yaml.deserializers.FilterOperatorDeserializer;
import org.dotwebstack.orchestrate.parser.yaml.deserializers.MatcherDeserializer;
import org.dotwebstack.orchestrate.parser.yaml.deserializers.ModelLoaderDeserializer;
import org.dotwebstack.orchestrate.parser.yaml.deserializers.ResultCombinerDeserializer;
import org.dotwebstack.orchestrate.parser.yaml.deserializers.ResultMapperDeserializer;
import org.dotwebstack.orchestrate.parser.yaml.mixins.ModelMappingMixin;
import org.dotwebstack.orchestrate.parser.yaml.mixins.PathMappingMixin;
import org.dotwebstack.orchestrate.parser.yaml.mixins.PropertyMappingMixin;

public final class YamlModelMappingParser {

  public static final String SOURCE_MODELS_KEY = "sourceModels";

  private final YAMLMapper yamlMapper = new YAMLMapper();

  public YamlModelMappingParser(ComponentRegistry componentRegistry, ModelLoaderRegistry modelLoaderRegistry,
      ValueTypeRegistry valueTypeRegistry) {
    var module = new SimpleModule()
        .setMixInAnnotation(ModelMapping.ModelMappingBuilder.class, ModelMappingMixin.class)
        .setMixInAnnotation(PropertyMapping.PropertyMappingBuilder.class, PropertyMappingMixin.class)
        .setMixInAnnotation(PathMapping.PathMappingBuilder.class, PathMappingMixin.class)
        .addDeserializer(Model.class, new ModelLoaderDeserializer(modelLoaderRegistry, valueTypeRegistry))
        .addDeserializer(ResultCombiner.class, new ResultCombinerDeserializer(componentRegistry))
        .addDeserializer(ResultMapper.class, new ResultMapperDeserializer(componentRegistry))
        .addDeserializer(Matcher.class, new MatcherDeserializer(componentRegistry))
        .addDeserializer(FilterOperator.class, new FilterOperatorDeserializer(componentRegistry))
        .addDeserializer(Cardinality.class, new CardinalityDeserializer());

    yamlMapper.registerModule(module);
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
