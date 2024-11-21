package nl.geostandaarden.imx.orchestrate.engine.fetch.stage;

import nl.geostandaarden.imx.orchestrate.engine.exchange.ObjectResult;
import reactor.core.publisher.Mono;

@FunctionalInterface
public interface NextStageCreator {

    Mono<Stage> create(ObjectResult result);
}
