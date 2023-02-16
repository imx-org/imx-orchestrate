package org.dotwebstack.orchestrate;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.graphql.test.tester.HttpGraphQlTester;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GatewayApplicationTest {

  private static final String QUERY = """
        {
          adres(identificatie: "0200200000075716") {
            identificatie
            huisnummer
            huisletter
            huisnummertoevoeging
            postcode
            straatnaam
            plaatsnaam
            isHoofdadres
            omschrijving
            hasLineage {
              orchestratedProperties {
                property
                value {
                  stringValue
                  integerValue
                  booleanValue
                }
                isDerivedFrom {
                  property
                  propertyPath
                  subject {
                    objectType
                    objectKey
                  }
                  value {
                    stringValue
                    integerValue
                    booleanValue
                  }
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
        .path("adres")
        .entity(Map.class)
        .get();

    assertThat(adres).isNotNull()
        .containsEntry("identificatie", "0200200000075716");
  }
}
