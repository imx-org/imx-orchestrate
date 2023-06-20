package nl.kadaster.gdc.orchestrate.mapper;

import graphql.ExecutionInput;
import java.util.List;
import java.util.Map;
import java.util.Set;
import nl.kadaster.gdc.orchestrate.config.GraphQlOrchestrateConfig;
import org.dotwebstack.orchestrate.model.Attribute;
import org.dotwebstack.orchestrate.model.ObjectType;
import org.dotwebstack.orchestrate.source.ObjectRequest;
import org.dotwebstack.orchestrate.source.SelectedProperty;
import org.dotwebstack.orchestrate.source.SourceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static nl.kadaster.gdc.orchestrate.mapper.GraphQlAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ObjectGraphQlMapperTest {

  private ObjectGraphQlMapper objectGraphQlMapper;

  @BeforeEach
  void init() {
    var config = GraphQlOrchestrateConfig.builder()
        .build();
    objectGraphQlMapper = new ObjectGraphQlMapper(config);
  }

  @Test
  void convert_returnsExpectedResult_forRequest() {
    var naam = new SelectedProperty(Attribute.builder()
        .name("naam")
        .build());

    var straat = new SelectedProperty(Attribute.builder()
        .name("straat")
        .build());
    var huisnummer = new SelectedProperty(Attribute.builder()
        .name("huisnummer")
        .build());

    var adres = new SelectedProperty(Attribute.builder()
        .name("adres")
        .build(), Set.of(straat, huisnummer));

    var request = ObjectRequest.builder()
        .objectKey(Map.of("identificatie", "12345", "id", "456"))
        .objectType(ObjectType.builder()
            .name("Nummeraanduiding")
            .build())
        .selectedProperties(List.of(naam, adres))
        .build();

    ExecutionInput result = objectGraphQlMapper.convert(request);

    var expected = """
      query Query {
        nummeraanduiding(identificatie: "12345", id: "456") {
          naam
          adres {
            straat
            huisnummer
          }
        }
      }""";

    assertThat(result.getQuery()).graphQlEquals(expected);
  }

  @Test
  void convert_throwsException_forRequest_withoutSelectionSet() {
    var request = ObjectRequest.builder()
      .objectKey(Map.of("identificatie", "12345"))
      .objectType(ObjectType.builder()
        .name("Nummeraanduiding")
        .build())
      .build();

    assertThrows(SourceException.class, () -> objectGraphQlMapper.convert(request));
  }
}
