package org.dotwebstack.orchestrate.engine.fetch;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

@Getter
@Builder(toBuilder = true)
public class CollectionResult implements Result {

  @Singular
  private final List<ObjectResult> objectResults;

  public List<Map<String, Object>> toList(ObjectMapper objectMapper, UnaryOperator<String> lineageRenamer) {
    return objectResults.stream()
        .map(objectResult -> objectResult.toMap(objectMapper, lineageRenamer))
        .toList();
  }
}
