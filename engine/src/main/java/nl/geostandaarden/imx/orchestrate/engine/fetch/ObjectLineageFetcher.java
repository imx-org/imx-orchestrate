package nl.geostandaarden.imx.orchestrate.engine.fetch;

import static nl.geostandaarden.imx.orchestrate.engine.fetch.FetchUtils.cast;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import nl.geostandaarden.imx.orchestrate.engine.OrchestrateException;
import nl.geostandaarden.imx.orchestrate.engine.schema.SchemaConstants;
import nl.geostandaarden.imx.orchestrate.model.lineage.ObjectLineage;

@RequiredArgsConstructor
public class ObjectLineageFetcher implements DataFetcher<Map<String, Object>> {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
      .setSerializationInclusion(Include.NON_NULL);

  private final Map<String, String> nameMapping;

  @Override
  public Map<String, Object> get(DataFetchingEnvironment environment) {
    Map<String, Object> source = environment.getSource();
    var hasLineageValue = source.get(SchemaConstants.HAS_LINEAGE_FIELD);

    if (hasLineageValue instanceof ObjectLineage objectLineage) {
      return renameKeys(FetchUtils.cast(OBJECT_MAPPER.convertValue(objectLineage, Map.class)));
    }

    throw new OrchestrateException("Could not fetch object lineage.");
  }

  private Map<String, Object> renameKeys(Map<String, Object> map) {
    return map.entrySet()
        .stream()
        .collect(Collectors.toMap(entry -> nameMapping.getOrDefault(entry.getKey(), entry.getKey()),
            entry -> {
              var value = entry.getValue();

              if (value instanceof Map<?, ?> mapValue) {
                return renameKeys(FetchUtils.cast(mapValue));
              }

              if (value instanceof List<?> listValue) {
                return listValue.stream()
                    .map(item -> {
                      if (item instanceof Map<?, ?> mapItem) {
                        return renameKeys(FetchUtils.cast(mapItem));
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
