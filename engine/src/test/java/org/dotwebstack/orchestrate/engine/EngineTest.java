package org.dotwebstack.orchestrate.engine;

import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

class EngineTest {

  private final Engine engine = new Engine();

  @Test
  void execute_Succeeds_Always() {
    var result = engine.fetch();

    StepVerifier.create(result)
        .expectNextCount(1)
        .expectComplete();
  }
}
