package nl.geostandaarden.imx.orchestrate.source.graphql.executor;

import java.time.Duration;
import java.util.Optional;
import java.util.function.Consumer;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import nl.geostandaarden.imx.orchestrate.source.graphql.config.GraphQlOrchestrateConfig;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class GraphQlWebClient {

    static WebClient create(GraphQlOrchestrateConfig config) {
        Consumer<HttpHeaders> headerBuilder = headers -> Optional.ofNullable(config.getAuthToken())
                .ifPresent(bearerAuth -> headers.add("Authorization", "Bearer ".concat(String.valueOf(bearerAuth))));

        ConnectionProvider provider = ConnectionProvider.builder("orchestrate")
                .maxIdleTime(Duration.ofSeconds(10))
                .build();

        HttpClient client = HttpClient.create(provider);

        var webClientBuilder = WebClient.builder().clientConnector(new ReactorClientHttpConnector());

        return webClientBuilder
                .defaultHeaders(headerBuilder)
                .clientConnector(new ReactorClientHttpConnector(client))
                .exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(5 * 1024 * 1024))
                        .build())
                .baseUrl(config.getBaseUrl())
                .build();
    }
}
