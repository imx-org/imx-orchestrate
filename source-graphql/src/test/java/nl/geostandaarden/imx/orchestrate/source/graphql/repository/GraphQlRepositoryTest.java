package nl.geostandaarden.imx.orchestrate.source.graphql.repository;

import java.util.Map;
import nl.geostandaarden.imx.orchestrate.model.Attribute;
import nl.geostandaarden.imx.orchestrate.model.ObjectType;
import nl.geostandaarden.imx.orchestrate.source.BatchRequest;
import nl.geostandaarden.imx.orchestrate.source.CollectionRequest;
import nl.geostandaarden.imx.orchestrate.source.ObjectRequest;
import nl.geostandaarden.imx.orchestrate.source.SelectedProperty;
import nl.geostandaarden.imx.orchestrate.source.graphql.executor.Executor;
import nl.geostandaarden.imx.orchestrate.source.graphql.mapper.BatchGraphQlMapper;
import nl.geostandaarden.imx.orchestrate.source.graphql.mapper.CollectionGraphQlMapper;
import nl.geostandaarden.imx.orchestrate.source.graphql.mapper.ObjectGraphQlMapper;
import nl.geostandaarden.imx.orchestrate.source.graphql.mapper.ResponseMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class GraphQlRepositoryTest {

  @Mock
  private Executor executor;

  @Mock
  private ObjectGraphQlMapper objectGraphQlMapper;

  @Mock
  private CollectionGraphQlMapper collectionGraphQlMapper;

  @Mock
  private BatchGraphQlMapper batchGraphQlMapper;

  @Mock
  private ResponseMapper responseMapper;

  @InjectMocks
  private GraphQlRepository repository;

  @Test
  void findOne_usesCorrectMappers() {
    this.repository.findOne(getObjectRequest());

    verify(objectGraphQlMapper, times(1)).convert(any(ObjectRequest.class));
    verifyNoInteractions(collectionGraphQlMapper);
    verifyNoInteractions(batchGraphQlMapper);

    verify(responseMapper, times(1)).processFindOneResult(any());
    verify(responseMapper, never()).processFindResult(any(), any());
    verify(responseMapper, never()).processBatchResult(any(), any());
  }

  @Test
  void find_usesCorrectMappers() {
    this.repository.find(getCollectionRequest());

    verifyNoInteractions(objectGraphQlMapper);
    verify(collectionGraphQlMapper, times(1)).convert(any(CollectionRequest.class));
    verifyNoInteractions(batchGraphQlMapper);

    verify(responseMapper, never()).processFindOneResult(any());
    verify(responseMapper, times(1)).processFindResult(any(), any());
    verify(responseMapper, never()).processBatchResult(any(), any());
  }

  @Test
  void findBatch_usesCorrectMappers() {
    this.repository.findBatch(getBatchRequest());

    verifyNoInteractions(objectGraphQlMapper);
    verifyNoInteractions(collectionGraphQlMapper);
    verify(batchGraphQlMapper, times(1)).convert(any(BatchRequest.class));

    verify(responseMapper, never()).processFindOneResult(any());
    verify(responseMapper, never()).processFindResult(any(), any());
    verify(responseMapper, times(1)).processBatchResult(any(), any());
  }

  private ObjectRequest getObjectRequest() {
    return ObjectRequest.builder()
      .objectType(
        ObjectType.builder()
          .name("abc")
          .build())
      .objectKey(Map.of("id", "123"))
      .selectedProperty(new SelectedProperty(Attribute.builder().name("attr").build()))
      .build();
  }

  private CollectionRequest getCollectionRequest() {
    return CollectionRequest.builder()
      .objectType(
        ObjectType.builder()
          .name("abc")
          .build())
      .selectedProperty(new SelectedProperty(Attribute.builder().name("attr").build()))
      .build();
  }

  private BatchRequest getBatchRequest() {
    return BatchRequest.builder()
      .objectType(
        ObjectType.builder()
          .name("abc")
          .build())
      .selectedProperty(new SelectedProperty(Attribute.builder().name("attr").build()))
      .build();
  }

}
