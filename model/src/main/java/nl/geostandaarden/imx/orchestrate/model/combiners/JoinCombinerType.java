package nl.geostandaarden.imx.orchestrate.model.combiners;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import nl.geostandaarden.imx.orchestrate.model.lineage.PathExecution;
import nl.geostandaarden.imx.orchestrate.model.result.PathResult;
import nl.geostandaarden.imx.orchestrate.model.result.PropertyMappingResult;

public final class JoinCombinerType implements ResultCombinerType {

  @Override
  public String getName() {
    return "join";
  }

  @Override
  public ResultCombiner create(Map<String, Object> options) {
    return pathResults -> {
      var nonEmptyResults = pathResults
          .stream()
          .filter(PathResult::isNotNull)
          .toList();

      if (nonEmptyResults.isEmpty()) {
        return PropertyMappingResult.empty();
      }

      var value = nonEmptyResults.stream()
          .map(PathResult::getValue)
          .map(String::valueOf)
          .collect(Collectors.joining());

      var sourceDataElements = nonEmptyResults.stream()
          .map(PathResult::getPathExecution)
          .map(PathExecution::getReferences)
          .flatMap(Set::stream)
          .collect(Collectors.toSet());

      return PropertyMappingResult.builder()
          .value(value)
          .sourceDataElements(sourceDataElements)
          .build();
    };
  }
}
