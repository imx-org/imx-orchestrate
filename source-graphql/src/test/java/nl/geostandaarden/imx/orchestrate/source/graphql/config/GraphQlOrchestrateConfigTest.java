package nl.geostandaarden.imx.orchestrate.source.graphql.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class GraphQlOrchestrateConfigTest {

    @Test
    void build_returnsNewGraphQlOrchestrateConfig_forSuffixValues() {
        var result = GraphQlOrchestrateConfig
                .builder()
                .collectionSuffix("Collection")
                .batchSuffix("Batch")
                .build();

        assertThat(result.getAuthToken()).isNull();
        assertThat(result.getBaseUrl()).isNull();
        assertThat(result.getBatchSuffix()).hasToString("Batch");
        assertThat(result.getCollectionSuffix()).hasToString("Collection");
    }
}
