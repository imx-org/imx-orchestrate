package org.dotwebstack.orchestrate.parser.yaml;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import org.dotwebstack.orchestrate.model.ModelMapping;
import org.dotwebstack.orchestrate.model.PropertyMapping;
import org.dotwebstack.orchestrate.model.transforms.Transform;
import org.dotwebstack.orchestrate.model.transforms.TransformRegistry;
import org.dotwebstack.orchestrate.parser.yaml.config.PropertyMappingMixin;
import org.dotwebstack.orchestrate.parser.yaml.config.TransformDeserializer;

public class YamlModelMappingParser {

  private final ObjectMapper yamlObjectMapper;

  public static YamlModelMappingParser getInstance(TransformRegistry transformRegistry) {
    var module = new SimpleModule()
        .setMixInAnnotation(PropertyMapping.PropertyMappingBuilder.class, PropertyMappingMixin.class)
        .setDeserializerModifier(new BeanDeserializerModifier() {
          @Override
          public JsonDeserializer<?> modifyDeserializer(DeserializationConfig config, BeanDescription beanDesc,
                                                        JsonDeserializer<?> deserializer) {
            if (beanDesc.getBeanClass() == Transform.class) {
              return new TransformDeserializer(transformRegistry);
            }
            return deserializer;
          }
        });

    return new YamlModelMappingParser(new YAMLFactory(), Set.of(module));
  }

  private YamlModelMappingParser(YAMLFactory yamlFactory, Set<Module> modules) {
    yamlObjectMapper = new ObjectMapper(yamlFactory);
    modules.forEach(yamlObjectMapper::registerModule);
  }

  public ModelMapping parse(InputStream inputStream) {
    try {
      return yamlObjectMapper.readValue(inputStream, new TypeReference<>() {});
    } catch (IOException ioException) {
      throw new YamlModelMappingParserException(
          String.format("An error occurred while processing model mapping:%n%s", ioException.getMessage()),
          ioException);
    }
  }

}
