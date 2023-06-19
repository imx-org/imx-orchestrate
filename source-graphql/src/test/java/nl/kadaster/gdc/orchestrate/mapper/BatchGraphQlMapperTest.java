package nl.kadaster.gdc.orchestrate.mapper;

import nl.kadaster.gdc.orchestrate.config.GraphQlOrchestrateConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BatchGraphQlMapperTest {

  private BatchGraphQlMapper batchGraphQlMapper;

  @BeforeEach
  void init() {
    var config = GraphQlOrchestrateConfig.builder()
      .build();
    batchGraphQlMapper = new BatchGraphQlMapper(config);
  }

  @Test
  void convert_returnsExpectedResult() {

  }
}