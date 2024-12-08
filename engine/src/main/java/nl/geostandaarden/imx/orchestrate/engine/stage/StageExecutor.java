package nl.geostandaarden.imx.orchestrate.engine.stage;

import nl.geostandaarden.imx.orchestrate.engine.OrchestrateException;
import nl.geostandaarden.imx.orchestrate.engine.exchange.BatchRequest;
import nl.geostandaarden.imx.orchestrate.engine.exchange.CollectionRequest;
import nl.geostandaarden.imx.orchestrate.engine.exchange.ObjectRequest;
import nl.geostandaarden.imx.orchestrate.engine.exchange.ObjectResult;
import nl.geostandaarden.imx.orchestrate.engine.selection.BatchNode;
import nl.geostandaarden.imx.orchestrate.engine.selection.CollectionNode;
import nl.geostandaarden.imx.orchestrate.engine.selection.ObjectNode;
import reactor.core.publisher.Flux;
import reactor.util.function.Tuples;

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

        var request = ObjectRequest.builder() //
                .selection(selection)
                .build();

        return repository
                .findOne(request)
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

        var request = CollectionRequest.builder() //
                .selection(selection)
                .build();

        return repository
                .find(request)
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

        var request = BatchRequest.builder() //
                .selection(selection)
                .build();

        return repository
                .findBatch(request)
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
