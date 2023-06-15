package org.dotwebstack.orchestrate.parser.yaml;

import static org.assertj.core.api.Assertions.assertThat;

import org.dotwebstack.orchestrate.model.ComponentFactory;
import java.util.Optional;
import org.dotwebstack.orchestrate.model.Model;
import org.dotwebstack.orchestrate.model.loader.ModelLoader;
import org.dotwebstack.orchestrate.model.loader.ModelLoaderRegistry;
import org.junit.jupiter.api.Test;

class YamlModelMappingParserTest {

  @Test
  void mapWorks() {
    var modelLoaderRegistry = ModelLoaderRegistry.getInstance();
    modelLoaderRegistry.registerModelLoader(new ModelLoader() {
      @Override
      public String getProfile() {
        return "MIM";
      }

      @Override
      public Optional<Model> loadModel(String alias, String location) {
        return Optional.of(Model.builder().alias(alias).build());
      }
    });

    var yamlMapper = YamlModelMappingParser.getInstance(new ComponentFactory(), modelLoaderRegistry);
    var inputStream = YamlModelMappingParser.class.getResourceAsStream("/adresmapping.yaml");

    var mapping = yamlMapper.parse(inputStream);

    assertThat(mapping).isNotNull();
  }
}
