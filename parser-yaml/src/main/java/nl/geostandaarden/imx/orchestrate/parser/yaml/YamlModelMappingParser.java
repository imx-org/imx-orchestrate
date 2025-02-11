package nl.geostandaarden.imx.orchestrate.parser.yaml;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import java.io.IOException;
import java.io.InputStream;
import nl.geostandaarden.imx.orchestrate.model.ComponentRegistry;
import nl.geostandaarden.imx.orchestrate.model.ConditionalWhen;
import nl.geostandaarden.imx.orchestrate.model.Model;
import nl.geostandaarden.imx.orchestrate.model.ModelMapping;
import nl.geostandaarden.imx.orchestrate.model.Multiplicity;
import nl.geostandaarden.imx.orchestrate.model.PathMapping;
import nl.geostandaarden.imx.orchestrate.model.PropertyMapping;
import nl.geostandaarden.imx.orchestrate.model.combiners.ResultCombiner;
import nl.geostandaarden.imx.orchestrate.model.loader.ModelLoaderRegistry;
import nl.geostandaarden.imx.orchestrate.model.mappers.ResultMapper;
import nl.geostandaarden.imx.orchestrate.model.matchers.Matcher;
import nl.geostandaarden.imx.orchestrate.model.types.ValueTypeRegistry;
import nl.geostandaarden.imx.orchestrate.parser.yaml.deserializers.MatcherDeserializer;
import nl.geostandaarden.imx.orchestrate.parser.yaml.deserializers.ModelLoaderDeserializer;
import nl.geostandaarden.imx.orchestrate.parser.yaml.deserializers.MultiplicityDeserializer;
import nl.geostandaarden.imx.orchestrate.parser.yaml.deserializers.ResultCombinerDeserializer;
import nl.geostandaarden.imx.orchestrate.parser.yaml.deserializers.ResultMapperDeserializer;
import nl.geostandaarden.imx.orchestrate.parser.yaml.mixins.ConditionalWhenMixin;
import nl.geostandaarden.imx.orchestrate.parser.yaml.mixins.ModelMappingMixin;
import nl.geostandaarden.imx.orchestrate.parser.yaml.mixins.PathMappingMixin;
import nl.geostandaarden.imx.orchestrate.parser.yaml.mixins.PropertyMappingMixin;

public final class YamlModelMappingParser {

    public static final String SOURCE_MODELS_KEY = "sourceModels";

    private final YAMLMapper yamlMapper = new YAMLMapper();

    public YamlModelMappingParser(
            ComponentRegistry componentRegistry,
            ModelLoaderRegistry modelLoaderRegistry,
            ValueTypeRegistry valueTypeRegistry) {
        var module = new SimpleModule()
                .setMixInAnnotation(ModelMapping.ModelMappingBuilder.class, ModelMappingMixin.class)
                .setMixInAnnotation(PropertyMapping.PropertyMappingBuilder.class, PropertyMappingMixin.class)
                .setMixInAnnotation(ConditionalWhen.ConditionalWhenBuilder.class, ConditionalWhenMixin.class)
                .setMixInAnnotation(PathMapping.PathMappingBuilder.class, PathMappingMixin.class)
                .addDeserializer(Model.class, new ModelLoaderDeserializer(modelLoaderRegistry, valueTypeRegistry))
                .addDeserializer(ResultCombiner.class, new ResultCombinerDeserializer(componentRegistry))
                .addDeserializer(ResultMapper.class, new ResultMapperDeserializer(componentRegistry))
                .addDeserializer(Matcher.class, new MatcherDeserializer(componentRegistry))
                .addDeserializer(Multiplicity.class, new MultiplicityDeserializer());

        yamlMapper.registerModule(module);
    }

    public ModelMapping parse(InputStream inputStream) {
        try {
            return yamlMapper.readValue(inputStream, ModelMapping.class);
        } catch (IOException e) {
            throw new YamlModelMappingParserException(
                    String.format("An error occurred while processing model mapping:%n%s", e.getMessage()), e);
        }
    }
}
