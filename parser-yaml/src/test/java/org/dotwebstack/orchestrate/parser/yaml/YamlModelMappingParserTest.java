package org.dotwebstack.orchestrate.parser.yaml;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import org.dotwebstack.orchestrate.model.ComponentFactory;
import org.dotwebstack.orchestrate.model.Model;
import org.dotwebstack.orchestrate.model.loader.ModelLoader;
import org.dotwebstack.orchestrate.model.loader.ModelLoaderRegistry;
import org.junit.jupiter.api.Test;

class YamlModelMappingParserTest {

  @Test
  void parse_returnsModel_forValidFile() {
    var modelLoaderRegistry = ModelLoaderRegistry.getInstance();

    modelLoaderRegistry.registerModelLoader(new ModelLoader() {
      @Override
      public String getName() {
        return "mim";
      }

      @Override
      public Optional<Model> loadModel(String alias, String location) {
        return Optional.of(Model.builder().alias(alias).build());
      }
    });

    var modelMappingParser = YamlModelMappingParser.getInstance(new ComponentFactory(), modelLoaderRegistry);
    var inputStream = YamlModelMappingParser.class.getResourceAsStream("/mapping.yaml");
    var mapping = modelMappingParser.parse(inputStream);

    assertThat(mapping).isNotNull();
  }
}
