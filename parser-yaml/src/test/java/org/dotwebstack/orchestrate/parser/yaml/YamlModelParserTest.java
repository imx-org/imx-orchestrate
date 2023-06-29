package org.dotwebstack.orchestrate.parser.yaml;

import static org.assertj.core.api.Assertions.assertThat;

import org.dotwebstack.orchestrate.model.types.ValueTypeRegistry;
import org.junit.jupiter.api.Test;

class YamlModelParserTest {

  @Test
  void parse_returnsModel_forValidFile() {
    var modelParser = new YamlModelParser(new ValueTypeRegistry());
    var inputStream = YamlModelParser.class.getResourceAsStream("/buildings.yaml");
    var model = modelParser.parse(inputStream);

    assertThat(model).isNotNull();
  }
}
