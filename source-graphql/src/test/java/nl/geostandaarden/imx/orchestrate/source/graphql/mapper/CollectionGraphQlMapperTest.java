package nl.geostandaarden.imx.orchestrate.source.graphql.mapper;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import graphql.ExecutionInput;
import java.util.Map;
import nl.geostandaarden.imx.orchestrate.engine.exchange.CollectionRequest;
import nl.geostandaarden.imx.orchestrate.engine.source.SourceException;
import nl.geostandaarden.imx.orchestrate.model.Attribute;
import nl.geostandaarden.imx.orchestrate.model.Model;
import nl.geostandaarden.imx.orchestrate.model.ObjectType;
import nl.geostandaarden.imx.orchestrate.model.ObjectTypeRef;
import nl.geostandaarden.imx.orchestrate.model.Path;
import nl.geostandaarden.imx.orchestrate.model.Relation;
import nl.geostandaarden.imx.orchestrate.model.filters.EqualsOperatorType;
import nl.geostandaarden.imx.orchestrate.model.filters.FilterExpression;
import nl.geostandaarden.imx.orchestrate.model.types.ScalarTypes;
import nl.geostandaarden.imx.orchestrate.source.graphql.config.GraphQlOrchestrateConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
    var request = CollectionRequest.builder(createModel())
        .objectType("Nummeraanduiding")
        .selectProperty("naam")
        .selectObjectProperty("adres", builder -> builder
            .selectProperty("straat")
            .selectProperty("huisnummer")
            .build())
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

    var request = CollectionRequest.builder(createModel())
        .objectType("Nummeraanduiding")
        .selectProperty("naam")
        .selectObjectProperty("adres", builder -> builder
            .selectProperty("straat")
            .selectProperty("huisnummer")
            .build())
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

    var request = CollectionRequest.builder(createModel())
        .objectType("Nummeraanduiding")
        .selectProperty("naam")
        .selectObjectProperty("adres", builder -> builder
            .selectProperty("straat")
            .selectProperty("huisnummer")
            .build())
        .filter(filterExpression)
        .build();

    assertThatThrownBy(() -> collectionGraphQlMapper.convert(request)).isInstanceOf(SourceException.class)
        .hasMessageContaining("Unknown filter operator 'unknown-operator'");
  }

  private Model createModel() {
    return Model.builder()
        .objectType(ObjectType.builder()
            .name("Nummeraanduiding")
            .property(Attribute.builder()
                .name("naam")
                .type(ScalarTypes.STRING)
                .build())
            .property(Relation.builder()
                .name("adres")
                .target(ObjectTypeRef.forType("Adres"))
                .build())
            .build())
        .objectType(ObjectType.builder()
            .name("Adres")
            .property(Attribute.builder()
                .name("straat")
                .type(ScalarTypes.STRING)
                .build())
            .property(Attribute.builder()
                .name("huisnummer")
                .type(ScalarTypes.INTEGER)
                .build())
            .build())
        .build();
  }
}
