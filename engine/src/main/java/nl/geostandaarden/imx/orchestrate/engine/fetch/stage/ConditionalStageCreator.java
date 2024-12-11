package nl.geostandaarden.imx.orchestrate.engine.fetch.stage;

import static nl.geostandaarden.imx.orchestrate.engine.fetch.FetchUtils.cast;
import static nl.geostandaarden.imx.orchestrate.model.ModelUtils.noopCombiner;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import lombok.Builder;
import lombok.Getter;
import nl.geostandaarden.imx.orchestrate.engine.OrchestrateException;
import nl.geostandaarden.imx.orchestrate.engine.exchange.ObjectResult;
import nl.geostandaarden.imx.orchestrate.engine.selection.TreeResolver;
import nl.geostandaarden.imx.orchestrate.model.ConditionalMapping;
import nl.geostandaarden.imx.orchestrate.model.ObjectTypeRef;
import nl.geostandaarden.imx.orchestrate.model.Path;
import nl.geostandaarden.imx.orchestrate.model.lineage.PathExecution;
import nl.geostandaarden.imx.orchestrate.model.result.PathResult;
import nl.geostandaarden.imx.orchestrate.model.result.PropertyMappingResult;
import reactor.core.publisher.Mono;

@Getter
@Builder(toBuilder = true)
public class ConditionalStageCreator implements NextStageCreator {

    private final ConditionalMapping conditionalMapping;

    private final ObjectTypeRef sourceRoot;

    private final Map<String, Object> objectKey;

    private final TreeResolver treeResolver;

    @Override
    public Mono<Stage> create(ObjectResult result) {
        var whenTrue = conditionalMapping.getWhen().stream().allMatch(when -> {
            var pathMappings = when.getPathMappings();

            var pathResults = pathMappings.stream()
                    .map(pathMapping -> {
                        var pathResult = traversePath(result.getProperties(), pathMapping.getPath());

                        return pathMapping.getResultMappers().stream()
                                .reduce(
                                        pathResult,
                                        (acc, resultMapper) -> resultMapper.apply(acc, null),
                                        noopCombiner());
                    })
                    .toList();

            if (pathMappings.size() == 1) {
                return evalCondition(pathResults.get(0).getValue(), when.getCondition());
            }

            if (when.getCombiner() != null) {
                var combinedResult = when.getCombiner().apply(pathResults);
                return evalCondition(combinedResult, when.getCondition());
            }

            return evalCondition(pathResults, when.getCondition());
        });

        if (whenTrue) {
            var then = conditionalMapping.getThen();

            if (then.getMapping() != null) {
                return Mono.just(new StagePlanner(treeResolver).plan(sourceRoot, objectKey, then.getMapping()));
            }

            if (then.getValues() != null) {
                return Mono.just(Stage.builder()
                        .nextResult(result.replaceProperties(then.getValues()))
                        .build());
            }

            throw new OrchestrateException("Mapping or values are required in 'else' construct.");
        }

        var otherwise = conditionalMapping.getOtherwise();

        if (otherwise.getMapping() != null) {
            return Mono.just(new StagePlanner(treeResolver).plan(sourceRoot, objectKey, otherwise.getMapping()));
        }

        if (otherwise.getValues() != null) {
            return Mono.just(Stage.builder()
                    .nextResult(result.withProperties(otherwise.getValues()))
                    .build());
        }

        throw new OrchestrateException("Mapping or values are required in 'otherwise' construct.");
    }

    private boolean evalCondition(Object value, Map<String, Object> condition) {
        if (value == null) {
            return false;
        }

        var evalValue = value instanceof PropertyMappingResult mappingResult ? mappingResult.getValue() : value;

        if (condition.containsKey("eq")) {
            return condition.get("eq").equals(evalValue);
        }

        if (condition.containsKey("neq")) {
            return !condition.get("neq").equals(evalValue);
        }

        if (condition.containsKey("gte") && evalValue instanceof Integer intValue) {
            return intValue >= ((Integer) condition.get("gte"));
        }

        if (condition.containsKey("lte") && evalValue instanceof Integer intValue) {
            return intValue <= ((Integer) condition.get("lte"));
        }

        if (condition.containsKey("in")) {
            Set<Object> inList = cast(condition.get("in"));
            return inList.contains(evalValue);
        }

        if (condition.containsKey("notIn")) {
            Set<Object> inList = cast(condition.get("in"));
            return !inList.contains(evalValue);
        }

        throw new OrchestrateException("Could not evaluate condition: " + condition);
    }

    private PathResult traversePath(Map<String, Object> properties, Path path) {
        var value = properties.get(path.getFirstSegment());

        if (path.isLeaf() || value == null) {
            return PathResult.builder() //
                    .pathExecution(PathExecution.builder().build())
                    .value(value)
                    .build();
        }

        if (value instanceof ObjectResult objectResult) {
            return traversePath(objectResult.getProperties(), path.withoutFirstSegment());
        }

        if (value instanceof Map<?, ?> mapValue) {
            return traversePath(cast(mapValue), path.withoutFirstSegment());
        }

        if (value instanceof Collection<?> collectionValue) {
            var collectionResult = collectionValue.stream()
                    .map(item -> {
                        if (item instanceof Map<?, ?> mapItem) {
                            return traversePath(cast(mapItem), path.withoutFirstSegment());
                        }

                        return item;
                    })
                    .toList();

            return PathResult.builder() //
                    .pathExecution(PathExecution.builder().build())
                    .value(collectionResult)
                    .build();
        }

        throw new OrchestrateException("Could not traverse path: " + path);
    }
}
