package org.dotwebstack.orchestrate.engine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.dotwebstack.orchestrate.engine.TestFixtures.createModelMapping;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import graphql.GraphQL;
import graphql.schema.idl.SchemaPrinter;
import java.util.Map;
import org.dotwebstack.orchestrate.engine.schema.SchemaFactory;
import org.dotwebstack.orchestrate.source.DataRepository;
import org.dotwebstack.orchestrate.source.ObjectRequest;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

class IntegrationTest {

  @Test
  void create() {
    var repositoryStub = createRepositoryStub();
    var orchestration = Orchestration.builder()
        .modelMapping(createModelMapping())
        .source("bag", () -> repositoryStub)
        .build();

    var schema = SchemaFactory.create(orchestration);
    System.out.println(new SchemaPrinter().print(schema));

    var graphQL = GraphQL.newGraphQL(schema)
        .build();

    var result = graphQL.execute("""
          query {
            adres(identificatie: "0200200000075716") {
              identificatie
              huisnummer
              postcode
              straatnaam
              plaatsnaam
            }
          }
        """);

    verify(repositoryStub, times(3)).findOne(any(ObjectRequest.class));

    assertThat(result).isNotNull();
    assertThat(result.getErrors()).isEmpty();
    assertThat(result.isDataPresent()).isTrue();

    Map<String, Object> data = result.getData();
    assertThat(data).isEqualTo(Map.of("adres", Map.of("identificatie", "0200200000075716", "huisnummer", 701,
        "postcode", "7334DP", "straatnaam", "Laan van Westenenk", "plaatsnaam", "Apeldoorn")));

    System.out.println(result);
  }

  private DataRepository createRepositoryStub() {
    var dataRepositoryMock = mock(DataRepository.class);

    when(dataRepositoryMock.findOne(any(ObjectRequest.class)))
        .thenAnswer(invocation -> {
          var objectRequest = (ObjectRequest) invocation.getArgument(0);
          var objectType = objectRequest.getObjectType();
          var objectkey = objectRequest.getObjectKey();

          return switch (objectType.getName()) {
            case "Nummeraanduiding":
              assertThat(objectRequest.getSelectedFields()).hasSize(5);
              assertThat(objectkey).isEqualTo(Map.of("identificatie", "0200200000075716"));
              yield Mono.just(Map.of("identificatie", "0200200000075716", "huisnummer", 701, "postcode", "7334DP",
                  "ligtAan", Map.of("identificatie", "0200300022472362")));
            case "OpenbareRuimte":
              assertThat(objectRequest.getSelectedFields()).hasSize(2);
              assertThat(objectkey).isEqualTo(Map.of("identificatie", "0200300022472362"));
              yield Mono.just(Map.of("naam", "Laan van Westenenk", "ligtIn", Map.of("identificatie", "3560")));
            case "Woonplaats":
              assertThat(objectRequest.getSelectedFields()).hasSize(1);
              assertThat(objectkey).isEqualTo(Map.of("identificatie", "3560"));
              yield Mono.just(Map.of("naam", "Apeldoorn"));
            default:
              yield Mono.error(() -> new RuntimeException("Error!"));
          };
        });

    return dataRepositoryMock;
  }
}
