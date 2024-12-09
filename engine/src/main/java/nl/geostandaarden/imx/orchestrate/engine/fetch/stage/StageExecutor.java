package nl.geostandaarden.imx.orchestrate.engine.fetch.stage;

import lombok.extern.slf4j.Slf4j;
import nl.geostandaarden.imx.orchestrate.engine.OrchestrateException;
import nl.geostandaarden.imx.orchestrate.engine.exchange.ObjectResult;
import nl.geostandaarden.imx.orchestrate.engine.selection.BatchNode;
import nl.geostandaarden.imx.orchestrate.engine.selection.CollectionNode;
import nl.geostandaarden.imx.orchestrate.engine.selection.ObjectNode;
import reactor.core.publisher.Flux;
import reactor.util.function.Tuples;

@Slf4j
public final class StageExecutor {

    public Flux<ObjectResult> execute(Stage stage) {
        var selection = stage.getSelection();

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
        var repository = selection.getSource().getDataRepository();

        log.debug(("\n=== ObjectRequest ===\n")
                .concat("Object type: " + selection.getObjectType().getName() + "\n")
                .concat("Object key: " + selection.getObjectKey() + "\n")
                .concat("Selection: " + selection + "\n"));

        return repository
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
                        }));
    }

    public Flux<ObjectResult> execute(Stage stage, CollectionNode selection) {
        var repository = selection.getSource().getDataRepository();

        log.debug(("\n=== CollectionRequest ===\n")
                .concat("Object type: " + selection.getObjectType().getName() + "\n")
                .concat("Filter: " + selection.getFilter() + "\n")
                .concat("Selection: " + selection + "\n"));

        return repository
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
                        }));
    }

    public Flux<ObjectResult> execute(Stage stage, BatchNode selection) {
        var repository = selection.getSource().getDataRepository();

        log.debug(("\n=== BatchRequest ===\n")
                .concat("Object type: " + selection.getObjectType().getName() + "\n")
                .concat("Object keys: " + selection.getObjectKeys() + "\n")
                .concat("Selection: " + selection + "\n"));

        return repository
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
                        }));
    }
}
