package org.dotwebstack.orchestrate.parser.yaml;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import org.dotwebstack.orchestrate.parser.yaml.model.FieldMappingMixin;
import org.dotwebstack.orchestrate.model.FieldMapping;
import org.dotwebstack.orchestrate.model.ModelMapping;

public class YamlModelMappingParser {

  private final ObjectMapper yamlObjectMapper;

  public static YamlModelMappingParser getInstance() {
    var module = new SimpleModule();
    module.setMixInAnnotation(FieldMapping.FieldMappingBuilder.class, FieldMappingMixin.class);
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
