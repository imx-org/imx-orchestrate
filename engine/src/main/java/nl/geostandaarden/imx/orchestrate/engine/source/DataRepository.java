package nl.geostandaarden.imx.orchestrate.engine.source;

import java.util.Map;
import nl.geostandaarden.imx.orchestrate.engine.exchange.BatchRequest;
import nl.geostandaarden.imx.orchestrate.engine.exchange.CollectionRequest;
import nl.geostandaarden.imx.orchestrate.engine.exchange.ObjectRequest;
import nl.geostandaarden.imx.orchestrate.model.ObjectType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface DataRepository {

    Mono<Map<String, Object>> findOne(ObjectRequest request);

    Flux<Map<String, Object>> find(CollectionRequest request);

    default Flux<Map<String, Object>> findBatch(BatchRequest request) {
        throw new SourceException("Batch loading not supported.");
    }

    default boolean supportsBatchLoading(ObjectType objectType) {
        return false;
    }
}
