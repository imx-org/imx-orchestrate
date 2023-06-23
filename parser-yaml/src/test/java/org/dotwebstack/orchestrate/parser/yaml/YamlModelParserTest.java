package org.dotwebstack.orchestrate.parser.yaml;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class YamlModelParserTest {

  private final YamlModelParser modelParser = YamlModelParser.getInstance();

  @Test
  void parse_returnsModel_forValidFile() {
    var inputStream = YamlModelParser.class.getResourceAsStream("/buildings.yaml");
    var model = modelParser.parse(inputStream);

    assertThat(model).isNotNull();
  }
}
