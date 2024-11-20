package nl.geostandaarden.imx.orchestrate.engine.fetch;

import java.util.List;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import nl.geostandaarden.imx.orchestrate.engine.OrchestrateException;
import nl.geostandaarden.imx.orchestrate.engine.exchange.CollectionRequest;
import nl.geostandaarden.imx.orchestrate.engine.exchange.ObjectResult;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

@Slf4j
@SuperBuilder(toBuilder = true)
public final class CollectionFetchOperation extends AbstractFetchOperation {

    private final FilterMapper filterMapper;

    public Flux<ObjectResult> fetch(FetchInput input) {
        var collectionRequest = CollectionRequest.builder(model)
                .objectType(objectType.getName())
                .filter(filterMapper != null ? filterMapper.apply(input) : null)
                .selectedProperties(selectedProperties)
                .build();

        if (log.isDebugEnabled()) {
            log.debug(collectionRequest.toString());
        }

        return source.getDataRepository().find(collectionRequest).map(properties -> ObjectResult.builder()
                .type(objectType)
                .properties(properties)
                .build());
    }

    @Override
    protected Publisher<ObjectResult> fetchBatch(List<FetchInput> inputs) {
        throw new OrchestrateException("Batch loading for collections is not (yet) supported.");
    }
}
