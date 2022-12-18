package org.dotwebstack.orchestrate.engine;

import org.dotwebstack.orchestrate.engine.fetch.FetchRequest;
import org.dotwebstack.orchestrate.engine.fetch.SelectedField;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

class EngineTest {

  @Test
  void execute_Succeeds_Always() {
    var modelMapping = EngineTestFixtures.createModelMapping();

    var request = FetchRequest.builder()
        .objectType("Area")
        .selectedField(SelectedField.builder()
            .name("code")
            .build())
        .selectedField(SelectedField.builder()
            .name("manager")
            .build())
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
