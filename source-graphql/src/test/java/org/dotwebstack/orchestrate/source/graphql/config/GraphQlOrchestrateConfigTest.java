package org.dotwebstack.orchestrate.source.graphql.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GraphQlOrchestrateConfigTest {

    @Test
    void build_returnsNewGraphQlOrchestrateConfig_forSuffixNullValues() {
        var result = GraphQlOrchestrateConfig
                .builder()
                .collectionSuffix(null)
                .batchSuffix(null)
                .build();

        assertThat(result.getAuthToken()).isNull();
        assertThat(result.getBaseUrl()).isNull();
        assertThat(result.getBatchSuffix()).isEmpty();
        assertThat(result.getCollectionSuffix()).isEmpty();
    }

    @Test
    void build_returnsNewGraphQlOrchestrateConfig_forSuffixNonNullValues() {
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
