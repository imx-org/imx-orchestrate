package org.dotwebstack.orchestrate.source.graphql;

import org.dotwebstack.orchestrate.source.SourceException;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GraphQlSourceTypeTest {

    @Test
    void getName_returnsName() {
        var result = new GraphQlSourceType().getName();

        assertThat(result).isEqualTo("graphql");
    }

    @Test
    void create_returnsNewSource() {
        Map<String, Object> config = Map.of("url",  "http://localhost:8080");

        var result = new GraphQlSourceType().create(null, config);

        assertThat(result).isNotNull();
        assertThat(result.getDataRepository()).isNotNull();
    }

    @Test
    void create_throwsException_forMissingUrl() {
        Map<String, Object> config = Map.of();
        var sourceType = new GraphQlSourceType();

        assertThatThrownBy(() -> sourceType.create(null, config)).isInstanceOf(SourceException.class)
                .hasMessageContaining("Config 'url' is missing.");
    }
}
