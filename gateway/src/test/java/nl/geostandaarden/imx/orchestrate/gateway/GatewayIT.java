package nl.geostandaarden.imx.orchestrate.gateway;

import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.graphql.test.tester.HttpGraphQlTester;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

@ActiveProfiles("it")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GatewayIT {

  private static final String QUERY = """
        {
          construction(id: "B0002") {
            id
            surface
            hasLineage {
              orchestratedDataElements {
                property
                wasDerivedFrom {
                  property
                }
              }
            }
          }
        }
      """;

  @Autowired
  private ApplicationContext applicationContext;

  private GraphQlTester graphQlTester;

  @BeforeEach
  void setUp() {
    graphQlTester = HttpGraphQlTester.create(WebTestClient.bindToApplicationContext(applicationContext)
        .configureClient()
        .baseUrl("/graphql")
        .build());
  }

  @Test
  void contextLoads_forDefaultProperties() {
    assertThat(applicationContext).isNotNull();
  }

  @Test
  @SuppressWarnings("unchecked")
  void queryReturnsResponse_forGraphQLMediaType() {
    Map<String, Object> adres = (Map<String, Object>) graphQlTester.document(QUERY)
        .execute()
        .path("construction")
        .entity(Map.class)
        .get();

    assertThat(adres).isNotNull()
        .containsEntry("id", "B0002")
        .containsEntry("surface", 195)
        .extractingByKey("hasLineage", as(InstanceOfAssertFactories.MAP))
        .extractingByKey("orchestratedDataElements", as(InstanceOfAssertFactories.COLLECTION))
        .hasSize(2);
  }
}
