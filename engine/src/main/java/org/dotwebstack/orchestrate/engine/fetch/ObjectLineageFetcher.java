package org.dotwebstack.orchestrate.engine.fetch;

import static org.dotwebstack.orchestrate.engine.fetch.FetchUtils.cast;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.dotwebstack.orchestrate.engine.schema.SchemaConstants;

@RequiredArgsConstructor
public class ObjectLineageFetcher implements DataFetcher<Map<String, Object>> {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
      .setSerializationInclusion(Include.NON_NULL);

  private final Map<String, String> nameMapping;

  @Override
  public Map<String, Object> get(DataFetchingEnvironment environment) {
    Map<String, Object> source = environment.getSource();
    var objectLineage = source.get(SchemaConstants.HAS_LINEAGE_FIELD);
    return renameKeys(cast(OBJECT_MAPPER.convertValue(objectLineage, Map.class)));
  }

  private Map<String, Object> renameKeys(Map<String, Object> map) {
    return map.entrySet()
        .stream()
        .collect(Collectors.toMap(entry -> nameMapping.getOrDefault(entry.getKey(), entry.getKey()),
            entry -> {
              var value = entry.getValue();

              if (value instanceof Map<?, ?> mapValue) {
                return renameKeys(cast(mapValue));
              }

              if (value instanceof List<?> listValue) {
                return listValue.stream()
                    .map(item -> {
                      if (item instanceof Map<?, ?> mapItem) {
                        return renameKeys(cast(mapItem));
                      }

                      return item;
                    })
                    .toList();
              }

              return value;
            }
        ));
  }
}
