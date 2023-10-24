package nl.geostandaarden.imx.orchestrate.engine.fetch;

import nl.geostandaarden.imx.orchestrate.engine.exchange.DataResult;
import reactor.core.publisher.Mono;

@FunctionalInterface
public interface FetchPlan<T extends DataResult> {

  Mono<T> execute();
}
