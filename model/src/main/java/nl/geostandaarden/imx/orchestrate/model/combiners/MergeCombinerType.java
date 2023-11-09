package nl.geostandaarden.imx.orchestrate.model.combiners;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import nl.geostandaarden.imx.orchestrate.model.lineage.PathExecution;
import nl.geostandaarden.imx.orchestrate.model.result.PathResult;
import nl.geostandaarden.imx.orchestrate.model.result.PropertyMappingResult;

public final class MergeCombinerType implements ResultCombinerType {

  @Override
  public String getName() {
    return "merge";
  }

  @Override
  public ResultCombiner create(Map<String, Object> options) {
    return pathResults -> {
      var value = pathResults.stream()
          .filter(PathResult::isNotNull)
          .flatMap(pathResult -> {
            var pathValue = pathResult.getValue();

//            if (pathValue instanceof CollectionResult collectionResult) {
//              return collectionResult.getObjectResults()
//                  .stream();
//            }

            return Stream.of(pathValue);
          })
          .toList();

      var sourceDataElements = pathResults.stream()
          .filter(PathResult::isNotNull)
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
