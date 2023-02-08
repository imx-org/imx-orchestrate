package org.dotwebstack.orchestrate.parser.yaml;

import static org.assertj.core.api.Assertions.assertThat;
import java.util.Objects;
import org.dotwebstack.orchestrate.model.transforms.Coalesce;
import org.dotwebstack.orchestrate.model.transforms.TestPredicate;
import org.dotwebstack.orchestrate.model.MappingRegistry;
import org.junit.jupiter.api.Test;

class YamlModelMappingParserTest {

  @Test
  void mapWorks() {
    var transformRegistry = MappingRegistry.builder()
        .register(Coalesce.getInstance())
        .registerTransform(TestPredicate.builder()
            .name("nonNull")
            .predicate(Objects::nonNull)
            .build())
        .build();

    var yamlMapper = YamlModelMappingParser.getInstance(transformRegistry);
    var inputStream = YamlModelMappingParser.class.getResourceAsStream("/adresmapping.yaml");

    var mapping = yamlMapper.parse(inputStream);

    assertThat(mapping).isNotNull();
  }
}
