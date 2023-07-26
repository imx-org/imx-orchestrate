package nl.geostandaarden.imx.orchestrate.parser.yaml;

import static org.assertj.core.api.Assertions.assertThat;

import nl.geostandaarden.imx.orchestrate.model.ComponentRegistry;
import nl.geostandaarden.imx.orchestrate.model.Model;
import nl.geostandaarden.imx.orchestrate.model.loader.ModelLoader;
import nl.geostandaarden.imx.orchestrate.model.loader.ModelLoaderRegistry;
import nl.geostandaarden.imx.orchestrate.model.types.ValueTypeRegistry;
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
