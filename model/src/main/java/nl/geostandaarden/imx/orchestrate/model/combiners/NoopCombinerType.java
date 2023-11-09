package nl.geostandaarden.imx.orchestrate.model.combiners;

import static java.util.stream.Collectors.toSet;

import java.util.Map;
import java.util.Set;

import nl.geostandaarden.imx.orchestrate.model.lineage.PathExecution;
import nl.geostandaarden.imx.orchestrate.model.result.PathResult;
import nl.geostandaarden.imx.orchestrate.model.result.PropertyMappingResult;

public final class NoopCombinerType implements ResultCombinerType {

  @Override
  public String getName() {
    return "noop";
  }

  @Override
  public ResultCombiner create(Map<String, Object> options) {
    return pathResults -> {
      var nonEmptyResults = pathResults
          .stream()
          .filter(PathResult::isNotNull)
          .toList();

      return PropertyMappingResult.builder()
          .value(nonEmptyResults.stream()
              .map(PathResult::getValue)
              .toList())
          .sourceDataElements(nonEmptyResults.stream()
              .map(PathResult::getPathExecution)
              .map(PathExecution::getReferences)
              .flatMap(Set::stream)
              .collect(toSet()))
          .build();
    };
  }
}
