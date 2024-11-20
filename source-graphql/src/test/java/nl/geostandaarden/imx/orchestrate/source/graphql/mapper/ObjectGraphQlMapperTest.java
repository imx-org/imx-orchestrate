package nl.geostandaarden.imx.orchestrate.source.graphql.mapper;

import static org.junit.jupiter.api.Assertions.assertThrows;

import graphql.ExecutionInput;
import java.util.Map;
import nl.geostandaarden.imx.orchestrate.engine.exchange.ObjectRequest;
import nl.geostandaarden.imx.orchestrate.engine.source.SourceException;
import nl.geostandaarden.imx.orchestrate.model.Attribute;
import nl.geostandaarden.imx.orchestrate.model.Model;
import nl.geostandaarden.imx.orchestrate.model.ObjectType;
import nl.geostandaarden.imx.orchestrate.model.ObjectTypeRef;
import nl.geostandaarden.imx.orchestrate.model.Relation;
import nl.geostandaarden.imx.orchestrate.model.types.ScalarTypes;
import nl.geostandaarden.imx.orchestrate.source.graphql.config.GraphQlOrchestrateConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ObjectGraphQlMapperTest {

    private ObjectGraphQlMapper objectGraphQlMapper;

    @BeforeEach
    void init() {
        var config = GraphQlOrchestrateConfig.builder().build();
        objectGraphQlMapper = new ObjectGraphQlMapper(config);
    }

    @Test
    void convert_returnsExpectedResult_forRequest() {
        var request = ObjectRequest.builder(createModel())
                .objectKey(Map.of("identificatie", "12345", "id", "456"))
                .objectType("Nummeraanduiding")
                .selectProperty("naam")
                .selectObjectProperty("adres", builder -> builder.selectProperty("straat")
                        .selectProperty("huisnummer")
                        .build())
                .build();

        ExecutionInput result = objectGraphQlMapper.convert(request);

        var expected =
                """
        query Query {
          nummeraanduiding(identificatie: "12345", id: "456") {
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
    void convert_throwsException_forRequest_withoutSelectionSet() {
        var request = ObjectRequest.builder(createModel())
                .objectKey(Map.of("identificatie", "12345"))
                .objectType("Nummeraanduiding")
                .build();

        assertThrows(SourceException.class, () -> objectGraphQlMapper.convert(request));
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
