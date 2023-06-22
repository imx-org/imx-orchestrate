package org.dotwebstack.orchestrate.source.graphql.mapper;

import static org.dotwebstack.orchestrate.source.graphql.mapper.MapperConstants.NODES;
import static org.springframework.util.StringUtils.uncapitalize;

import graphql.ExecutionResult;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.dotwebstack.orchestrate.source.graphql.config.GraphQlOrchestrateConfig;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class ResponseMapper {

  private final GraphQlOrchestrateConfig config;

  public Mono<Map<String, Object>> processFindOneResult(Mono<ExecutionResult> executionResult) {
    return executionResult.map(result -> Objects.requireNonNullElse(result.getData(), Map.of()));
  }

  public Flux<Map<String, Object>> processFindResult(Mono<ExecutionResult> executionResult, String objectName) {
    return executionResult.map(e -> getCollectionResult(e, objectName))
        .flatMapMany(Flux::fromIterable);
  }

  public Flux<Map<String, Object>> processBatchResult(Mono<ExecutionResult> executionResult, String objectName) {
    return executionResult.map(e -> getBatchResult(e, objectName))
        .flatMapMany(Flux::fromIterable);
  }

  private List<Map<String, Object>> getCollectionResult(ExecutionResult executionResult, String objectName) {
    var data = executionResult.getData();
    var result = ((Map<String, Map<String, List<Map<String, Object>>>>) data)
        .get(uncapitalize(objectName) + config.getCollectionSuffix());
    return result.get(NODES);
  }

  private List<Map<String, Object>> getBatchResult(ExecutionResult executionResult, String objectName) {
    var data = executionResult.getData();
    return ((Map<String, List<Map<String, Object>>>) data).get(uncapitalize(objectName) + config.getBatchSuffix());

  }
}
