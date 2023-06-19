package nl.kadaster.gdc.orchestrate.repository;

import java.util.Map;
import nl.kadaster.gdc.orchestrate.config.GraphQlOrchestrateConfig;
import nl.kadaster.gdc.orchestrate.executor.Executor;
import nl.kadaster.gdc.orchestrate.executor.RemoteExecutor;
import nl.kadaster.gdc.orchestrate.mapper.BatchGraphQlMapper;
import nl.kadaster.gdc.orchestrate.mapper.CollectionGraphQlMapper;
import nl.kadaster.gdc.orchestrate.mapper.ObjectGraphQlMapper;
import nl.kadaster.gdc.orchestrate.mapper.ResponseMapper;
import org.dotwebstack.orchestrate.model.ObjectType;
import org.dotwebstack.orchestrate.source.BatchRequest;
import org.dotwebstack.orchestrate.source.CollectionRequest;
import org.dotwebstack.orchestrate.source.DataRepository;
import org.dotwebstack.orchestrate.source.DataRequest;
import org.dotwebstack.orchestrate.source.ObjectRequest;
import org.dotwebstack.orchestrate.source.SourceException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class GraphQlRepository implements DataRepository {

  private final Executor executor;

  private final ObjectGraphQlMapper objectGraphQlMapper;

  private final CollectionGraphQlMapper collectionGraphQlMapper;

  private final BatchGraphQlMapper batchGraphQlMapper;

  private final ResponseMapper responseMapper;

  public GraphQlRepository(GraphQlOrchestrateConfig config) {
    this.executor = RemoteExecutor.create(config);
    this.objectGraphQlMapper = new ObjectGraphQlMapper(config);
    this.collectionGraphQlMapper = new CollectionGraphQlMapper(config);
    this.batchGraphQlMapper = new BatchGraphQlMapper(config);
    this.responseMapper = new ResponseMapper(config);
  }

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
    if (!supportsBatchLoading(batchRequest.getObjectType())) {
      throw new SourceException(
          "Batch loading is not supported for objecttype %s.".formatted(batchRequest.getObjectType()
              .getName()));
    }
    var graphQl = batchGraphQlMapper.convert(batchRequest);

    return responseMapper.processBatchResult(this.executor.execute(graphQl), getName(batchRequest));
  }

  @Override
  public boolean supportsBatchLoading(ObjectType objectType) {
    return true;
  }

  private String getName(DataRequest request) {
    return request.getObjectType()
        .getName();
  }
}
