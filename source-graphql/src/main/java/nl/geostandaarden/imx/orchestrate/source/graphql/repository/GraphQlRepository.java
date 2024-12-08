package nl.geostandaarden.imx.orchestrate.source.graphql.repository;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import nl.geostandaarden.imx.orchestrate.engine.exchange.BatchRequest;
import nl.geostandaarden.imx.orchestrate.engine.exchange.CollectionRequest;
import nl.geostandaarden.imx.orchestrate.engine.exchange.DataRequest;
import nl.geostandaarden.imx.orchestrate.engine.exchange.ObjectRequest;
import nl.geostandaarden.imx.orchestrate.engine.source.DataRepository;
import nl.geostandaarden.imx.orchestrate.model.ObjectType;
import nl.geostandaarden.imx.orchestrate.source.graphql.executor.Executor;
import nl.geostandaarden.imx.orchestrate.source.graphql.mapper.BatchGraphQlMapper;
import nl.geostandaarden.imx.orchestrate.source.graphql.mapper.CollectionGraphQlMapper;
import nl.geostandaarden.imx.orchestrate.source.graphql.mapper.ObjectGraphQlMapper;
import nl.geostandaarden.imx.orchestrate.source.graphql.mapper.ResponseMapper;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class GraphQlRepository implements DataRepository {

    private final Executor executor;

    private final ObjectGraphQlMapper objectGraphQlMapper;

    private final CollectionGraphQlMapper collectionGraphQlMapper;

    private final BatchGraphQlMapper batchGraphQlMapper;

    private final ResponseMapper responseMapper;

    @Override
    public Mono<Map<String, Object>> findOne(ObjectRequest objectRequest) {
        var graphQl = objectGraphQlMapper.convert(objectRequest);
        return responseMapper.processFindOneResult(this.executor.execute(graphQl));
    }

    @Override
    public Flux<Map<String, Object>> find(CollectionRequest collectionRequest) {
        var graphQl = collectionGraphQlMapper.convert(collectionRequest);
        return responseMapper.processFindResult(this.executor.execute(graphQl), getName(collectionRequest));
    }

    @Override
    public Flux<Map<String, Object>> findBatch(BatchRequest batchRequest) {
        var graphQl = batchGraphQlMapper.convert(batchRequest);
        return responseMapper.processBatchResult(this.executor.execute(graphQl), getName(batchRequest));
    }

    @Override
    public boolean supportsBatchLoading(ObjectType objectType) {
        return true;
    }

    private String getName(DataRequest<?> request) {
        return request.getSelection() //
                .getObjectType()
                .getName();
    }
}
