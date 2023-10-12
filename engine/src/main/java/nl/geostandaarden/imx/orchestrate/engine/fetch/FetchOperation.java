package nl.geostandaarden.imx.orchestrate.engine.fetch;

import java.util.List;
import nl.geostandaarden.imx.orchestrate.engine.exchange.ObjectResult;
import reactor.core.publisher.Flux;

interface FetchOperation {

  Flux<ObjectResult> execute(FetchInput input);

  Flux<ObjectResult> executeBatch(List<FetchInput> inputs);
}
