package nl.kadaster.gdc.orchestrate;

import com.google.auto.service.AutoService;
import java.util.Map;
import nl.kadaster.gdc.orchestrate.config.GraphQlOrchestrateConfig;
import org.dotwebstack.orchestrate.model.Model;
import org.dotwebstack.orchestrate.source.Source;
import org.dotwebstack.orchestrate.source.SourceException;
import org.dotwebstack.orchestrate.source.SourceType;

@AutoService(SourceType.class)
public class GraphQlSourceType implements SourceType {

  private static final String SOURCE_TYPE = "graphql";

  private static final String BEARER_TOKEN = "bearerToken";

  private static final String COLLECTION_SUFFIX = "collectionSuffix";

  private static final String BATCH_SUFFIX = "batchSuffix";

  private static final String URL_KEY = "url";

  @Override
  public String getName() {
    return SOURCE_TYPE;
  }

  @Override
  public Source create(Model model, Map<String, Object> options) {
    validate(options);

    var config = createConfig(options);
    return new GraphQlSource(config);
  }

  private void validate(Map<String, Object> options) {
    if (!options.containsKey(URL_KEY)) {
      throw new SourceException(String.format("Config '%s' is missing.", URL_KEY));
    }
  }

  private static GraphQlOrchestrateConfig createConfig(Map<String, Object> options) {
    return GraphQlOrchestrateConfig.builder()
        .authToken(((String) options.get(BEARER_TOKEN)).toCharArray())
        .baseUrl((String) options.get(URL_KEY))
        .collectionSuffix((String) options.get(COLLECTION_SUFFIX))
        .batchSuffix((String) options.get(BATCH_SUFFIX))
        .build();
  }
}
