package org.dotwebstack.orchestrate.engine.fetch;

import static java.util.Collections.unmodifiableMap;
import static org.dotwebstack.orchestrate.engine.fetch.FetchUtils.keyExtractor;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import java.util.function.UnaryOperator;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import org.dotwebstack.orchestrate.engine.OrchestrateException;
import org.dotwebstack.orchestrate.engine.schema.SchemaConstants;
import org.dotwebstack.orchestrate.model.ObjectType;
import org.dotwebstack.orchestrate.model.lineage.ObjectLineage;

@Getter
@Builder(toBuilder = true)
public class ObjectResult implements Result {

  private final ObjectType type;

  @Singular
  private final Map<String, Object> properties;

  @Singular
  private final Map<String, Result> nestedResults;

  private final ObjectLineage lineage;

  public Map<String, Object> getKey() {
    return keyExtractor(type).apply(this);
  }

  public Object getProperty(String name) {
    return properties.get(name);
  }

  public Result getNestedResult(String name) {
    return nestedResults.get(name);
  }

  public Map<String, Object> toMap(ObjectMapper objectMapper, UnaryOperator<String> lineageRenamer) {
    var resultMap = new HashMap<>(properties);

    nestedResults.forEach((name, nestedResult) -> {
      if (nestedResult instanceof CollectionResult collectionResult) {
        resultMap.put(name, collectionResult.toList(objectMapper, lineageRenamer));
      } else if (nestedResult instanceof ObjectResult objectResult) {
        resultMap.put(name, objectResult.toMap(objectMapper, lineageRenamer));
      } else {
        throw new OrchestrateException("Could not map nested result.");
      }
    });

    var mappedLineage = objectMapper.convertValue(lineage, Object.class);
    resultMap.put(lineageRenamer.apply(SchemaConstants.HAS_LINEAGE_FIELD), mappedLineage);

    return unmodifiableMap(resultMap);
  }
}
