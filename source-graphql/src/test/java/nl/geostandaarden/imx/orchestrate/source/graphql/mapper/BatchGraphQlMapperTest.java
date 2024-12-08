package nl.geostandaarden.imx.orchestrate.source.graphql.mapper;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;
import nl.geostandaarden.imx.orchestrate.engine.selection.SelectionBuilder;
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
        var selection = SelectionBuilder.newBatchNode(createModel(), "Nummeraanduiding")
                .objectKey(Map.of("identificatie", "12345"))
                .objectKey(Map.of("identificatie", "34567"))
                .select("naam")
                .selectObject("adres", builder -> builder //
                        .select("straat")
                        .select("huisnummer")
                        .build())
                .build();

        var request = selection.toRequest();
        var result = batchGraphQlMapper.convert(request);

        var expected =
                """
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
        var selection = SelectionBuilder.newBatchNode(createModel(), "Nummeraanduiding")
                .objectKey(Map.of("identificatie", "12345"))
                .objectKey(Map.of("id", "34567"))
                .select("naam")
                .selectObject("adres", builder -> builder //
                        .select("straat")
                        .select("huisnummer")
                        .build())
                .build();

        var request = selection.toRequest();

        assertThrows(SourceException.class, () -> batchGraphQlMapper.convert(request));
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
