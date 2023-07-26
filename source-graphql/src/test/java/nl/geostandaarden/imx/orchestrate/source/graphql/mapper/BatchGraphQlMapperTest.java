package nl.geostandaarden.imx.orchestrate.source.graphql.mapper;

import graphql.ExecutionInput;
import java.util.List;
import java.util.Map;
import java.util.Set;
import nl.geostandaarden.imx.orchestrate.source.BatchRequest;
import nl.geostandaarden.imx.orchestrate.source.SelectedProperty;
import nl.geostandaarden.imx.orchestrate.source.SourceException;
import nl.geostandaarden.imx.orchestrate.source.graphql.config.GraphQlOrchestrateConfig;
import nl.geostandaarden.imx.orchestrate.model.Attribute;
import nl.geostandaarden.imx.orchestrate.model.ObjectType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class BatchGraphQlMapperTest {

  private BatchGraphQlMapper batchGraphQlMapper;

  @BeforeEach
  void init() {
    var config = GraphQlOrchestrateConfig.builder()
      .collectionSuffix("Collectie")
      .batchSuffix("Batch")
      .build();
    batchGraphQlMapper = new BatchGraphQlMapper(config);
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

    var request = BatchRequest.builder()
      .objectKey(Map.of("identificatie", "12345"))
      .objectKey(Map.of("identificatie", "34567"))
      .objectType(ObjectType.builder()
        .name("Nummeraanduiding")
        .build())
      .selectedProperties(List.of(naam, adres))
      .build();

    ExecutionInput result = batchGraphQlMapper.convert(request);

    var expected = """
      query Query {
        nummeraanduidingBatch(identificatie: ["12345", "34567"]) {
          naam
          adres {
            straat
            huisnummer
          }
        }
      }""";

    GraphQlAssert.assertThat(result.getQuery()).graphQlEquals(expected);
  }

  @Test
  void convert_throwsException_forRequest_withMoreThanOneKeyProperty() {
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

    var request = BatchRequest.builder()
      .objectKey(Map.of("identificatie", "12345"))
      .objectKey(Map.of("id", "34567"))
      .objectType(ObjectType.builder()
        .name("Nummeraanduiding")
        .build())
      .selectedProperties(List.of(naam, adres))
      .build();

    assertThrows(SourceException.class, () -> batchGraphQlMapper.convert(request));
  }

}