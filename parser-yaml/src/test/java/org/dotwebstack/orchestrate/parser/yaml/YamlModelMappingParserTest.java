package org.dotwebstack.orchestrate.parser.yaml;

import static org.assertj.core.api.Assertions.assertThat;

import org.dotwebstack.orchestrate.model.ComponentRegistry;
import org.dotwebstack.orchestrate.model.Model;
import org.dotwebstack.orchestrate.model.loader.ModelLoader;
import org.dotwebstack.orchestrate.model.loader.ModelLoaderRegistry;
import org.dotwebstack.orchestrate.model.types.ValueTypeRegistry;
import org.junit.jupiter.api.Test;

class YamlModelMappingParserTest {

  @Test
  void parse_returnsModel_forValidFile() {
    var modelLoaderRegistry = new ModelLoaderRegistry();

    modelLoaderRegistry.register(new ModelLoader() {
      @Override
      public String getName() {
        return "custom";
      }

      @Override
      public Model load(String location, ValueTypeRegistry valueTypeRegistry) {
        return Model.builder()
            .build();
      }
    });

    var modelMappingParser = new YamlModelMappingParser(new ComponentRegistry(), modelLoaderRegistry,
        new ValueTypeRegistry());
    var inputStream = YamlModelMappingParser.class.getResourceAsStream("/mapping.yaml");
    var mapping = modelMappingParser.parse(inputStream);

    assertThat(mapping).isNotNull();
  }
}
