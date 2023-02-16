package org.dotwebstack.orchestrate;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.graphql.execution.GraphQlSource;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GatewayApplicationTest {

  @Autowired
  private GraphQlSource graphQlSource;

  @Test
  void contextLoads() {
    assertThat(graphQlSource).isNotNull();
  }
}
