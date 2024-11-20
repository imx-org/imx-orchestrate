package nl.geostandaarden.imx.orchestrate.source.graphql.config;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GraphQlOrchestrateConfig {

    private char[] authToken;

    private String baseUrl;

    @Builder.Default
    private String collectionSuffix = "Collection";

    @Builder.Default
    private String batchSuffix = "Batch";
}
