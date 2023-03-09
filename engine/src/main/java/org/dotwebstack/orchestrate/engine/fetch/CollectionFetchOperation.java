package org.dotwebstack.orchestrate.engine.fetch;

import java.util.List;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import org.dotwebstack.orchestrate.engine.OrchestrateException;
import org.dotwebstack.orchestrate.source.CollectionRequest;
import org.dotwebstack.orchestrate.source.FilterDefinition;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

@Slf4j
@SuperBuilder(toBuilder = true)
final class CollectionFetchOperation extends AbstractFetchOperation {

  private final FilterDefinition filter;

  public Flux<ObjectResult> fetch(FetchInput input) {
    var collectionRequest = CollectionRequest.builder()
        .objectType(objectType)
        .filter(filter != null ? filter.createExpression(input.getData()) : null)
        .selectedProperties(selectedProperties)
        .build();

    if (log.isDebugEnabled()) {
      log.debug(collectionRequest.toString());
    }

    return source.getDataRepository()
        .find(collectionRequest)
        .map(properties -> ObjectResult.builder()
            .type(objectType)
            .properties(properties)
            .build());
  }

  @Override
  protected Publisher<ObjectResult> fetchBatch(List<FetchInput> inputs) {
    throw new OrchestrateException("Batch loading for connections is not (yet) supported.");
  }
}
