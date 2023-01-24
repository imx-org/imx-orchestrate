package org.dotwebstack.orchestrate.engine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.dotwebstack.orchestrate.engine.TestFixtures.createModelMapping;

import graphql.GraphQL;
import graphql.schema.idl.SchemaPrinter;
import java.util.Map;
import org.dotwebstack.orchestrate.engine.schema.SchemaFactory;
import org.dotwebstack.orchestrate.source.DataRepository;
import org.dotwebstack.orchestrate.source.Source;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

class IntegrationTest {

  @Test
  void create() {
    var schema = new SchemaFactory().create(createModelMapping(), Map.of("bag", createSourceMock()));
    System.out.println(new SchemaPrinter().print(schema));

    var graphQL = GraphQL.newGraphQL(schema)
        .build();

    var result = graphQL.execute("""
          query {
            adres {
              identificatie
              huisnummer
              postcode
              straatnaam
              plaatsnaam
            }
          }
        """);

    assertThat(result).isNotNull();
    assertThat(result.getErrors()).isEmpty();
    assertThat(result.isDataPresent()).isTrue();

    Map<String, Object> data = result.getData();
    assertThat(data).isEqualTo(Map.of("adres", Map.of("identificatie", "0200200000075716", "huisnummer", 701,
        "postcode", "7334DP", "straatnaam", "Laan van Westenenk", "plaatsnaam", "Apeldoorn")));

    System.out.println(result);
  }

  private Source createSourceMock() {
    return () -> (DataRepository) objectRequest -> {
      var typeName = objectRequest.getObjectType()
          .getName();

      return switch (typeName) {
        case "Nummeraanduiding" ->
            Mono.just(Map.of("identificatie", "0200200000075716", "huisnummer", 701, "postcode", "7334DP", "ligtAan",
                Map.of("identificatie", "0200300022472362")));
        case "OpenbareRuimte" ->
            Mono.just(Map.of("identificatie", "0200300022472362", "naam", "Laan van Westenenk", "ligtIn", Map.of(
                "identificatie", "3560")));
        case "Woonplaats" -> Mono.just(Map.of("identificatie", "3560", "naam", "Apeldoorn"));
        default -> Mono.error(() -> new RuntimeException("Error!"));
      };
    };
  }
}
