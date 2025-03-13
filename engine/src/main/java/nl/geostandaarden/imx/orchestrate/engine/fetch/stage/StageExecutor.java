package nl.geostandaarden.imx.orchestrate.engine.fetch.stage;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.geostandaarden.imx.orchestrate.engine.OrchestrateException;
import nl.geostandaarden.imx.orchestrate.engine.exchange.ObjectResult;
import nl.geostandaarden.imx.orchestrate.engine.selection.BatchNode;
import nl.geostandaarden.imx.orchestrate.engine.selection.CollectionNode;
import nl.geostandaarden.imx.orchestrate.engine.selection.ObjectNode;
import nl.geostandaarden.imx.orchestrate.engine.source.Source;
import reactor.core.publisher.Flux;
import reactor.util.function.Tuples;

@Slf4j
@RequiredArgsConstructor
public final class StageExecutor {

    private final Map<String, Source> sources;

    public Flux<ObjectResult> execute(Stage stage) {
        var selection = stage.getSelection();

        if (!stage.getNextResults().isEmpty()) {
            return Flux.fromIterable(stage.getNextResults());
        }

        if (selection instanceof ObjectNode objectNode) {
            return execute(stage, objectNode);
        }

        if (selection instanceof CollectionNode collectionNode) {
            return execute(stage, collectionNode);
        }

        if (selection instanceof BatchNode batchNode) {
            return execute(stage, batchNode);
        }

        throw new OrchestrateException("Could not execute request: " + selection.getClass());
    }

    public Flux<ObjectResult> execute(Stage stage, ObjectNode selection) {
        var source = sources.get(selection.getModelAlias());

        log.debug(("\n=== ObjectRequest ===\n")
                .concat("Object type: " + selection.getModelAlias() + ":"
                        + selection.getObjectType().getName() + "\n")
                .concat("Object key: " + selection.getObjectKey() + "\n")
                .concat("Selection: " + selection + "\n"));

        return source.getDataRepository()
                .findOne(selection.toRequest())
                .map(properties -> ObjectResult.builder()
                        .type(selection.getObjectType())
                        .properties(properties)
                        .build())
                .flatMapMany(result -> stage.getNextStages(result)
                        .flatMap(nextStage -> execute(nextStage)
                                .map(nextResult -> Tuples.of(nextResult, nextStage.getNextResultCombiner())))
                        .reduce(result, (accResult, nextResultTuple) -> {
                            var nextResultCombiner = nextResultTuple.getT2();
                            return nextResultCombiner.combine(accResult, nextResultTuple.getT1());
                        }))
                .flatMap(result -> stage.getConditionalStages(result)
                        .flatMap(nextStage -> execute(nextStage)
                                .map(nextResult -> Tuples.of(nextResult, nextStage.getNextResultCombiner())))
                        .reduce(result, (accResult, nextResultTuple) -> {
                            var nextResultCombiner = nextResultTuple.getT2();
                            return nextResultCombiner.combine(accResult, nextResultTuple.getT1());
                        }));
    }

    public Flux<ObjectResult> execute(Stage stage, CollectionNode selection) {
        var source = sources.get(selection.getModelAlias());

        log.debug(("\n=== CollectionRequest ===\n")
                .concat("Object type: " + selection.getModelAlias() + ":"
                        + selection.getObjectType().getName() + "\n")
                .concat("Filter: " + selection.getFilter() + "\n")
                .concat("Selection: " + selection + "\n"));

        return source.getDataRepository()
                .find(selection.toRequest())
                .map(properties -> ObjectResult.builder()
                        .type(selection.getObjectType())
                        .properties(properties)
                        .build())
                .flatMap(result -> stage.getNextStages(result)
                        .flatMap(nextStage -> execute(nextStage)
                                .map(nextResult -> Tuples.of(nextResult, nextStage.getNextResultCombiner())))
                        .reduce(result, (accResult, nextResultTuple) -> {
                            var nextResultCombiner = nextResultTuple.getT2();
                            return nextResultCombiner.combine(accResult, nextResultTuple.getT1());
                        }))
                .flatMap(result -> stage.getConditionalStages(result)
                        .flatMap(nextStage -> execute(nextStage)
                                .map(nextResult -> Tuples.of(nextResult, nextStage.getNextResultCombiner())))
                        .reduce(result, (accResult, nextResultTuple) -> {
                            var nextResultCombiner = nextResultTuple.getT2();
                            return nextResultCombiner.combine(accResult, nextResultTuple.getT1());
                        }));
    }

    public Flux<ObjectResult> execute(Stage stage, BatchNode selection) {
        var source = sources.get(selection.getModelAlias());

        log.debug(("\n=== BatchRequest ===\n")
                .concat("Object type: " + selection.getModelAlias() + ":"
                        + selection.getObjectType().getName() + "\n")
                .concat("Object keys: " + selection.getObjectKeys() + "\n")
                .concat("Selection: " + selection + "\n"));

        return source.getDataRepository()
                .findBatch(selection.toRequest())
                .map(properties -> ObjectResult.builder()
                        .type(selection.getObjectType())
                        .properties(properties)
                        .build())
                .flatMap(result -> stage.getNextStages(result)
                        .flatMap(nextStage -> execute(nextStage)
                                .map(nextResult -> Tuples.of(nextResult, nextStage.getNextResultCombiner())))
                        .reduce(result, (accResult, nextResultTuple) -> {
                            var nextResultCombiner = nextResultTuple.getT2();
                            return nextResultCombiner.combine(accResult, nextResultTuple.getT1());
                        }))
                .flatMap(result -> stage.getConditionalStages(result)
                        .flatMap(nextStage -> execute(nextStage)
                                .map(nextResult -> Tuples.of(nextResult, nextStage.getNextResultCombiner())))
                        .reduce(result, (accResult, nextResultTuple) -> {
                            var nextResultCombiner = nextResultTuple.getT2();
                            return nextResultCombiner.combine(accResult, nextResultTuple.getT1());
                        }));
    }
}
