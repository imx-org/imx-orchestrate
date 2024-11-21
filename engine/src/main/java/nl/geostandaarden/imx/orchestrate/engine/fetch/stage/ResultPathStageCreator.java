package nl.geostandaarden.imx.orchestrate.engine.fetch.stage;

import lombok.Builder;
import lombok.Getter;
import nl.geostandaarden.imx.orchestrate.engine.exchange.ObjectResult;
import nl.geostandaarden.imx.orchestrate.engine.selection.ObjectNode;
import nl.geostandaarden.imx.orchestrate.model.Path;
import reactor.core.publisher.Mono;

@Getter
@Builder
public class ResultPathStageCreator implements NextStageCreator {

    private final StagePlanner stagePlanner;

    private final Path resultPath;

    private final ObjectNode selection;

    @Override
    public Mono<Stage> create(ObjectResult result) {
        var stage = stagePlanner.plan(selection.toBuilder() //
                .objectKey(result.getKey())
                .build());

        return Mono.just(stage);
    }
}
