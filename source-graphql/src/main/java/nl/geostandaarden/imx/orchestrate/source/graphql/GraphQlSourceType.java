package nl.geostandaarden.imx.orchestrate.source.graphql;

import com.google.auto.service.AutoService;
import java.util.Map;
import nl.geostandaarden.imx.orchestrate.engine.source.Source;
import nl.geostandaarden.imx.orchestrate.engine.source.SourceException;
import nl.geostandaarden.imx.orchestrate.engine.source.SourceType;
import nl.geostandaarden.imx.orchestrate.model.Model;
import nl.geostandaarden.imx.orchestrate.source.graphql.config.GraphQlOrchestrateConfig;

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
        var configBuilder =
                GraphQlOrchestrateConfig.builder().baseUrl(options.get(URL_KEY).toString());

        if (options.containsKey(BEARER_TOKEN)) {
            configBuilder.authToken(options.get(BEARER_TOKEN).toString().toCharArray());
        }

        if (options.containsKey(COLLECTION_SUFFIX)) {
            configBuilder.collectionSuffix(options.get(COLLECTION_SUFFIX).toString());
        }

        if (options.containsKey(BATCH_SUFFIX)) {
            configBuilder.batchSuffix(options.get(BATCH_SUFFIX).toString());
        }

        return configBuilder.build();
    }
}
