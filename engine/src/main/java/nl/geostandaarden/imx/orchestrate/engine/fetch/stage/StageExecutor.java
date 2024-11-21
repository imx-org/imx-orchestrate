package nl.geostandaarden.imx.orchestrate.engine.fetch.stage;

import nl.geostandaarden.imx.orchestrate.engine.OrchestrateException;
import nl.geostandaarden.imx.orchestrate.engine.exchange.ObjectRequest;
import nl.geostandaarden.imx.orchestrate.engine.exchange.ObjectResult;
import reactor.core.publisher.Mono;

public class StageExecutor {

    public Mono<ObjectResult> execute(Stage stage) {
        var request = stage.getRequest();

        if (request instanceof ObjectRequest objectRequest) {
            return execute(stage, objectRequest);
        }

        throw new OrchestrateException("Could not execute request: " + request.getClass());
    }

    private Mono<ObjectResult> execute(Stage stage, ObjectRequest request) {
        var selection = request.getSelection();
        var repository = selection.getSource().getDataRepository();

        return repository.findOne(request).flatMap(properties -> {
            var result = ObjectResult.builder()
                    .type(selection.getObjectType())
                    .properties(properties)
                    .build();

            return stage.getNextStages(result).flatMap(this::execute).reduce(result, this::combineNextResult);
        });
    }

    private ObjectResult combineNextResult(ObjectResult result, ObjectResult nextResult) {
        return result;
    }
}
