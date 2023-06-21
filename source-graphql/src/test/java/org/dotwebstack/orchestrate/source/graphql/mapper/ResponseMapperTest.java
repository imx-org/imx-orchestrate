package org.dotwebstack.orchestrate.source.graphql.mapper;

import graphql.ExecutionResult;
import org.dotwebstack.orchestrate.source.graphql.config.GraphQlOrchestrateConfig;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class ResponseMapperTest {

    @Test
    void processFindOneResult_returnsMono() {
        var config = GraphQlOrchestrateConfig.builder().build();
        var executionResult = Mono.just(ExecutionResult.newExecutionResult().data(Map.of("testKey", "testValue")).build());

        var result = new ResponseMapper(config).processFindOneResult(executionResult);

        assertThat(result.block()).isEqualTo(Map.of("testKey", "testValue"));
    }

    @Test
    void processFindOneResult_returnsMono_forNull() {
        var config = GraphQlOrchestrateConfig.builder().build();
        var executionResult = Mono.just(ExecutionResult.newExecutionResult().data(null).build());

        var result = new ResponseMapper(config).processFindOneResult(executionResult);

        assertThat(result.block()).isEqualTo(Map.of());
    }

    @Test
    void processFindResult_returnsFlux() {
        var config = GraphQlOrchestrateConfig.builder().build();
        var executionResult = Mono.just(ExecutionResult.newExecutionResult().data(Map.of("test",
                Map.of("nodes", List.of(Map.of("testKey", "testValue"))))).build());

        var result = new ResponseMapper(config).processFindResult(executionResult, "Test");

        assertThat(result.blockFirst()).isEqualTo(Map.of("testKey", "testValue"));
    }

    @Test
    void processFindResult_returnsFlux_withSuffix() {
        var config = GraphQlOrchestrateConfig.builder().collectionSuffix("Collection").build();
        var executionResult = Mono.just(ExecutionResult.newExecutionResult().data(Map.of("testCollection",
                Map.of("nodes", List.of(Map.of("testKey", "testValue"))))).build());

        var result = new ResponseMapper(config).processFindResult(executionResult, "Test");

        assertThat(result.blockFirst()).isEqualTo(Map.of("testKey", "testValue"));
    }

    @Test
    void processBatchResult_returnsFlux() {
        var config = GraphQlOrchestrateConfig.builder().build();
        var executionResult = Mono.just(ExecutionResult.newExecutionResult().data(Map.of("test",
                List.of(Map.of("testKey", "testValue")))).build());

        var result = new ResponseMapper(config).processBatchResult(executionResult, "Test");

        assertThat(result.blockFirst()).isEqualTo(Map.of("testKey", "testValue"));
    }

    @Test
    void processBatchResult_returnsFlux_withSuffix() {
        var config = GraphQlOrchestrateConfig.builder().batchSuffix("Batch").build();
        var executionResult = Mono.just(ExecutionResult.newExecutionResult().data(Map.of("testBatch",
                List.of(Map.of("testKey", "testValue")))).build());

        var result = new ResponseMapper(config).processBatchResult(executionResult, "Test");

        assertThat(result.blockFirst()).isEqualTo(Map.of("testKey", "testValue"));
    }
}
