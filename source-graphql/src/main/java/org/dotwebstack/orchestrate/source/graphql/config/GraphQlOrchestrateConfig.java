package org.dotwebstack.orchestrate.source.graphql.config;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;

@Data
@Builder
@Setter(AccessLevel.NONE)
public class GraphQlOrchestrateConfig {

  private char[] authToken;

  private String baseUrl;

  private String collectionSuffix;

  private String batchSuffix;

  private GraphQlOrchestrateConfig(char[] authToken, String baseUrl, String collectionSuffix, String batchSuffix) {
    this.authToken = authToken;
    this.baseUrl = baseUrl;
    this.collectionSuffix = collectionSuffix == null ? "" : collectionSuffix;
    this.batchSuffix = batchSuffix == null ? "" : batchSuffix;
  }
}
