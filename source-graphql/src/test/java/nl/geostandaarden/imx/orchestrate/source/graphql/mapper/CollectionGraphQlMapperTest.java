package nl.geostandaarden.imx.orchestrate.source.graphql.mapper;

import graphql.ExecutionInput;
import nl.geostandaarden.imx.orchestrate.source.CollectionRequest;
import nl.geostandaarden.imx.orchestrate.source.SelectedProperty;
import nl.geostandaarden.imx.orchestrate.source.SourceException;
import nl.geostandaarden.imx.orchestrate.source.graphql.config.GraphQlOrchestrateConfig;
import nl.geostandaarden.imx.orchestrate.model.Attribute;
import nl.geostandaarden.imx.orchestrate.model.ObjectType;
import nl.geostandaarden.imx.orchestrate.model.Path;
import nl.geostandaarden.imx.orchestrate.model.filters.EqualsOperatorType;
import nl.geostandaarden.imx.orchestrate.model.filters.FilterExpression;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CollectionGraphQlMapperTest {

    private CollectionGraphQlMapper collectionGraphQlMapper;

    @BeforeEach
    void init() {
        var config = GraphQlOrchestrateConfig.builder()
                .collectionSuffix("Collection")
                .build();
        collectionGraphQlMapper = new CollectionGraphQlMapper(config);
    }

    @Test
    void convert_returnsExpectedResult_withoutFilter() {
        var request = CollectionRequest.builder()
                .objectType(ObjectType.builder()
                        .name("Nummeraanduiding")
                        .build())
                .selectedProperties(createSelectedProperties())
                .build();

        ExecutionInput result = collectionGraphQlMapper.convert(request);

        var expected = """
          query Query {
            nummeraanduidingCollection {
              nodes {
                naam
                adres {
                  straat
                  huisnummer
                }
              }
            }
          }""";

        GraphQlAssert.assertThat(result.getQuery()).graphQlEquals(expected);
    }

    @Test
    void convert_returnsExpectedResult_withFilter() {
        var filterExpression = FilterExpression.builder()
                .value("Kerkstraat")
                .operator(new EqualsOperatorType().create(Map.of()))
                .path(Path.fromString("adres/straat"))
                .build();
        var request = CollectionRequest.builder()
                .objectType(ObjectType.builder()
                        .name("Nummeraanduiding")
                        .build())
                .selectedProperties(createSelectedProperties())
                .filter(filterExpression)
                .build();

        ExecutionInput result = collectionGraphQlMapper.convert(request);

        var expected = """
          query Query {
            nummeraanduidingCollection(filter: {adres: { straat: { eq: "Kerkstraat" }}}) {
              nodes {
                naam
                adres {
                  straat
                  huisnummer
                }
              }
            }
          }""";

        GraphQlAssert.assertThat(result.getQuery()).graphQlEquals(expected);
    }

    @Test
    void convert_throwsException_forUnknownOperator() {
        var filterExpression = FilterExpression.builder()
                .value("Kerkstraat")
                .operator(() -> "unknown-operator")
                .path(Path.fromString("adres/straat"))
                .build();
        var request = CollectionRequest.builder()
                .objectType(ObjectType.builder()
                        .name("Nummeraanduiding")
                        .build())
                .selectedProperties(createSelectedProperties())
                .filter(filterExpression)
                .build();

        assertThatThrownBy(() -> collectionGraphQlMapper.convert(request)).isInstanceOf(SourceException.class)
                .hasMessageContaining("Unknown filter operator 'unknown-operator'");
    }

    private List<SelectedProperty> createSelectedProperties() {
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

        return List.of(naam, adres);
    }
}
