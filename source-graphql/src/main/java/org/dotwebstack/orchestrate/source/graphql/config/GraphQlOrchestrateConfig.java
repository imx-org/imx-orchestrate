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

  public static class GraphQlOrchestrateConfigBuilder {

    public GraphQlOrchestrateConfigBuilder collectionSuffix(String collectionSuffix) {
      this.collectionSuffix = (collectionSuffix == null ? "" : collectionSuffix);
      return this;
    }

    public GraphQlOrchestrateConfigBuilder batchSuffix(String batchSuffix) {
      this.batchSuffix = (batchSuffix == null ? "" : batchSuffix);
      return this;
    }
  }
}
