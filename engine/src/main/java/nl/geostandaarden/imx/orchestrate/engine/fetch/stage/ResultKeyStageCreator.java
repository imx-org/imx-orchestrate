package nl.geostandaarden.imx.orchestrate.engine.fetch.stage;

import static nl.geostandaarden.imx.orchestrate.engine.fetch.FetchUtils.cast;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Getter;
import nl.geostandaarden.imx.orchestrate.engine.OrchestrateException;
import nl.geostandaarden.imx.orchestrate.engine.exchange.CollectionResult;
import nl.geostandaarden.imx.orchestrate.engine.exchange.DataResult;
import nl.geostandaarden.imx.orchestrate.engine.exchange.ObjectResult;
import nl.geostandaarden.imx.orchestrate.engine.selection.BatchNode;
import nl.geostandaarden.imx.orchestrate.engine.selection.CollectionNode;
import nl.geostandaarden.imx.orchestrate.engine.selection.CompoundNode;
import nl.geostandaarden.imx.orchestrate.engine.selection.ObjectNode;
import nl.geostandaarden.imx.orchestrate.engine.selection.TreeResolver;
import nl.geostandaarden.imx.orchestrate.model.InverseRelation;
import nl.geostandaarden.imx.orchestrate.model.Path;
import nl.geostandaarden.imx.orchestrate.model.Relation;
import nl.geostandaarden.imx.orchestrate.model.filters.FilterExpression;
import reactor.core.publisher.Mono;

@Getter
@Builder(toBuilder = true)
public class ResultKeyStageCreator implements NextStageCreator {

    private final Path resultPath;

    private final CompoundNode selection;

    private final TreeResolver treeResolver;

    @Override
    public Mono<Stage> create(ObjectResult result) {
        if (selection instanceof ObjectNode objectNode) {
            var objectKey = resolveObjectKey(result);

            if (selection.getRelation() instanceof Relation) {
                var nextSelection = objectNode.toBuilder() //
                        .objectKey(objectKey)
                        .build();

                return Mono.just(new StagePlanner(treeResolver).plan(nextSelection, createNextResultCombiner(false)));
            }

            if (selection.getRelation() instanceof InverseRelation inverseRelation) {
                var filter = FilterExpression.builder()
                        .path(Path.fromString(
                                inverseRelation.getOriginRelation().getName()))
                        .value(objectKey)
                        .build();

                var nextSelection = CollectionNode.builder()
                        .relation(inverseRelation)
                        .childNodes(selection.getChildNodes())
                        .objectType(objectNode.getObjectType())
                        .modelAlias(selection.getModelAlias())
                        .filter(filter)
                        .build();

                return Mono.just(new StagePlanner(treeResolver).plan(nextSelection, createNextResultCombiner(false)));
            }
        }

        if (selection instanceof CollectionNode collectionNode) {
            if (selection.getRelation() instanceof Relation relation) {
                // TODO: Make sure key is selected + support longer paths
                List<Map<String, Object>> objectKeys = cast(result.getProperty(resultPath.getFirstSegment()));

                if (objectKeys == null || objectKeys.isEmpty()) {
                    return Mono.empty();
                }

                if (objectKeys.size() > 1) {
                    var nextSelection = BatchNode.builder()
                            .relation(relation)
                            .childNodes(selection.getChildNodes())
                            .objectType(collectionNode.getObjectType())
                            .modelAlias(selection.getModelAlias())
                            .objectKeys(objectKeys)
                            .build();

                    return Mono.just(
                            new StagePlanner(treeResolver).plan(nextSelection, createNextResultCombiner(true)));
                }

                var nextSelection = ObjectNode.builder()
                        .relation(relation)
                        .childNodes(selection.getChildNodes())
                        .objectType(collectionNode.getObjectType())
                        .modelAlias(selection.getModelAlias())
                        .objectKey(objectKeys.get(0))
                        .build();

                return Mono.just(new StagePlanner(treeResolver).plan(nextSelection, createNextResultCombiner(true)));
            }

            if (selection.getRelation() instanceof InverseRelation inverseRelation) {
                var filter = FilterExpression.builder()
                        .path(Path.fromString(
                                inverseRelation.getOriginRelation().getName()))
                        .value(result.getKey())
                        .build();

                var nextSelection = collectionNode.toBuilder() //
                        .filter(filter)
                        .build();

                return Mono.just(new StagePlanner(treeResolver).plan(nextSelection, createNextResultCombiner(true)));
            }
        }

        throw new OrchestrateException("Could not create stage.");
    }

    @Override
    public Mono<Stage> create(CollectionResult result) {
        // TODO
        throw new UnsupportedOperationException();
    }

    private NextResultCombiner createNextResultCombiner(boolean collection) {
        return new NextResultCombiner() {
            @Override
            public ObjectResult combine(ObjectResult result, DataResult nextResult) {
                var name = resultPath.getFirstSegment();
                var properties = new HashMap<>(result.getProperties());

                if (nextResult instanceof ObjectResult objectResult) {
                    if (collection) {
                        properties.put(
                                name,
                                CollectionResult.builder() //
                                        .objectResult(objectResult)
                                        .build());
                    } else {
                        properties.put(name, objectResult);
                    }
                } else if (nextResult instanceof CollectionResult collectionResult) {
                    if (collection) {
                        properties.put(name, collectionResult);
                    } else if (!collectionResult.getObjectResults().isEmpty()) {
                        properties.put(name, collectionResult.getObjectResults().get(0));
                    }
                } else {
                    throw new OrchestrateException("Could not combine result: " + nextResult.getClass());
                }

                return result.toBuilder() //
                        .properties(properties)
                        .build();
            }

            @Override
            public CollectionResult combine(CollectionResult result, DataResult nextResult) {
                // TODO
                throw new UnsupportedOperationException();
            }
        };
    }

    private Map<String, Object> resolveObjectKey(ObjectResult result) {
        var keyMapping = selection.getRelation().getKeyMapping();

        if (keyMapping != null) {
            return keyMapping.entrySet().stream() //
                    .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, entry -> {
                        // TODO: Support complex paths
                        return result.getProperty(entry.getValue().getFirstSegment());
                    }));
        }

        return result.getKey();
    }
}
