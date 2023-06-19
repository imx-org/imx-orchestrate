package nl.kadaster.gdc.orchestrate.config;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GraphQlOrchestrateConfig {

  private char[] authToken;

  private String baseUrl;

  private String collectionSuffix;

  private String batchSuffix;
}
