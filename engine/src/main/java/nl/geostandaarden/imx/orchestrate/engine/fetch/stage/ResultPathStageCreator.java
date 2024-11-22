package nl.geostandaarden.imx.orchestrate.engine.fetch.stage;

import java.util.HashMap;
import lombok.Builder;
import lombok.Getter;
import nl.geostandaarden.imx.orchestrate.engine.OrchestrateException;
import nl.geostandaarden.imx.orchestrate.engine.exchange.CollectionResult;
import nl.geostandaarden.imx.orchestrate.engine.exchange.DataResult;
import nl.geostandaarden.imx.orchestrate.engine.exchange.ObjectResult;
import nl.geostandaarden.imx.orchestrate.engine.selection.ObjectNode;
import nl.geostandaarden.imx.orchestrate.model.Path;
import reactor.core.publisher.Mono;

@Getter
@Builder
public class ResultPathStageCreator implements NextStageCreator {

    private final Path resultPath;

    private final ObjectNode selection;

    @Override
    public Mono<Stage> create(ObjectResult result) {
        var stage = new StagePlanner().plan(selection.toBuilder() //
                .objectKey(result.getKey())
                .build());

        return Mono.just(stage.toBuilder() //
                .nextResultCombiner(createNextResultCombiner())
                .build());
    }

    @Override
    public Mono<Stage> create(CollectionResult result) {
        // TODO: Add filter
        var stage = new StagePlanner().plan(selection.toBuilder() //
                .build());

        return Mono.just(stage.toBuilder() //
                .nextResultCombiner(createNextResultCombiner())
                .build());
    }

    private NextResultCombiner createNextResultCombiner() {
        return new NextResultCombiner() {
            @Override
            public ObjectResult combine(ObjectResult result, DataResult nextResult) {
                var name = resultPath.getFirstSegment();
                var properties = new HashMap<>(result.getProperties());

                if (nextResult instanceof ObjectResult objectResult) {
                    properties.put(name, objectResult.getProperties());
                } else if (nextResult instanceof CollectionResult collectionResult) {
                    properties.put(name, collectionResult.getPropertyList());
                } else {
                    throw new OrchestrateException("Could not combine result: " + nextResult.getClass());
                }

                return result.toBuilder() //
                        .properties(properties)
                        .build();
            }

            @Override
            public CollectionResult combine(CollectionResult result, DataResult nextResult) {
                return result;
            }
        };
    }
}
