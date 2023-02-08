package org.dotwebstack.orchestrate.parser.yaml;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;
import org.dotwebstack.orchestrate.model.ComponentRegistry;
import org.dotwebstack.orchestrate.model.combiners.Coalesce;
import org.dotwebstack.orchestrate.model.transforms.TestPredicate;
import org.junit.jupiter.api.Test;

class YamlModelMappingParserTest {

  @Test
  void mapWorks() {
    var componentRegistry = new ComponentRegistry()
        .registerTransform(TestPredicate.builder()
            .name("nonNull")
            .predicate(Objects::nonNull)
            .build())
        .registerCombiner(Coalesce.getInstance());

    var yamlMapper = YamlModelMappingParser.getInstance(componentRegistry);
    var inputStream = YamlModelMappingParser.class.getResourceAsStream("/adresmapping.yaml");

    var mapping = yamlMapper.parse(inputStream);

    assertThat(mapping).isNotNull();
  }
}
