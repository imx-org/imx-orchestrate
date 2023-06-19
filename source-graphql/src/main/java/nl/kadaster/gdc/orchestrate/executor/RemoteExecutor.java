package nl.kadaster.gdc.orchestrate.executor;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.ExecutionResultImpl;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import nl.kadaster.gdc.orchestrate.config.GraphQlOrchestrateConfig;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
public class RemoteExecutor implements Executor {

  private static final String DATA = "data";

  private final WebClient webClient;

  private RemoteExecutor(WebClient webClient) {
    this.webClient = webClient;
  }

  public static RemoteExecutor create(GraphQlOrchestrateConfig config) {
    return new RemoteExecutor(GraphQlWebClient.create(config));
  }

  @Override
  public Mono<ExecutionResult> execute(ExecutionInput input) {
    var mapTypeRef = new ParameterizedTypeReference<Map<String, Object>>() {};
    var body = Map.of("query", input.getQuery(), "variables", input.getVariables());

    if (log.isDebugEnabled()) {
      log.debug("Sending request: \n\n{}\n", input.getQuery());
    }

    return this.webClient.post()
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)
        .body(BodyInserters.fromValue(body))
        .retrieve()
        .bodyToMono(mapTypeRef)
        .map(RemoteExecutor::mapToResult);
  }

  private static ExecutionResult mapToResult(Map<String, Object> body) {
    return ExecutionResultImpl.newExecutionResult()
        .data(body.get(DATA))
        .build();
  }
}
