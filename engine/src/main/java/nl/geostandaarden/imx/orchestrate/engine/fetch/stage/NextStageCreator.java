package nl.geostandaarden.imx.orchestrate.engine.fetch.stage;

import nl.geostandaarden.imx.orchestrate.engine.exchange.BatchResult;
import nl.geostandaarden.imx.orchestrate.engine.exchange.CollectionResult;
import nl.geostandaarden.imx.orchestrate.engine.exchange.ObjectResult;
import reactor.core.publisher.Mono;

public interface NextStageCreator {

    default Mono<Stage> create(ObjectResult result) {
        return Mono.empty();
    }

    default Mono<Stage> create(CollectionResult result) {
        return Mono.empty();
    }

    default Mono<Stage> create(BatchResult result) {
        return Mono.empty();
    }
}
