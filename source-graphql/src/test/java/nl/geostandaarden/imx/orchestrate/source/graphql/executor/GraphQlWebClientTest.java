package nl.geostandaarden.imx.orchestrate.source.graphql.executor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.List;
import nl.geostandaarden.imx.orchestrate.source.graphql.config.GraphQlOrchestrateConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;

@MockitoSettings(strictness = Strictness.LENIENT)
class GraphQlWebClientTest {

    @Mock
    private ExchangeFunction exchangeFunction;

    @Captor
    private ArgumentCaptor<ClientRequest> captor;

    @BeforeEach
    public void setup() {
        ClientResponse mockResponse = mock();
        when(mockResponse.statusCode()).thenReturn(HttpStatus.OK);
        when(mockResponse.bodyToMono(Void.class)).thenReturn(Mono.empty());
        given(this.exchangeFunction.exchange(this.captor.capture())).willReturn(Mono.just(mockResponse));
    }

    @Test
    void create_returnsWebClient_withConfig() {
        var config = GraphQlOrchestrateConfig.builder()
                .authToken("123456790".toCharArray())
                .baseUrl("http://localhost:8080")
                .build();

        createWebClientAndFireRequest(config);
        var request = verifyAndGetRequest();

        assertThat(request.url().toString()).hasToString("http://localhost:8080/path");
        assertThat(request.headers()).containsKey("Authorization");
        assertThat(request.headers()).containsEntry("Authorization", List.of("Bearer 123456790"));
    }

    @Test
    void create_returnsWebClient_withoutConfig() {
        var config = GraphQlOrchestrateConfig.builder().build();

        createWebClientAndFireRequest(config);
        var request = verifyAndGetRequest();

        assertThat(request.url().toString()).hasToString("/path");
        assertThat(request.headers()).isEqualTo(new HttpHeaders());
    }

    private void createWebClientAndFireRequest(GraphQlOrchestrateConfig config) {
        GraphQlWebClient.create(config)
                .mutate()
                .exchangeFunction(this.exchangeFunction)
                .build()
                .get()
                .uri("/path")
                .retrieve()
                .bodyToMono(Void.class)
                .block(Duration.ofSeconds(10));
    }

    private ClientRequest verifyAndGetRequest() {
        ClientRequest request = this.captor.getValue();
        verify(this.exchangeFunction).exchange(request);
        verifyNoMoreInteractions(this.exchangeFunction);
        return request;
    }
}
