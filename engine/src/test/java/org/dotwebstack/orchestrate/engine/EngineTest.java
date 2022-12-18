package org.dotwebstack.orchestrate.engine;

import org.dotwebstack.orchestrate.engine.fetch.FetchRequest;
import org.dotwebstack.orchestrate.model.ModelMapping;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

class EngineTest {

  @Test
  void execute_Succeeds_Always() {
    var modelMapping = ModelMapping.builder()
        .build();
    var request = FetchRequest.builder()
        .build();
    var engine = Engine.builder()
        .modelMapping(modelMapping)
        .build();

    var result = engine.fetch(request);

    StepVerifier.create(result)
        .expectNextCount(1)
        .expectComplete();
  }
}
