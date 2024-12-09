package nl.geostandaarden.imx.orchestrate.source.graphql.repository;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.Map;
import nl.geostandaarden.imx.orchestrate.engine.exchange.BatchRequest;
import nl.geostandaarden.imx.orchestrate.engine.exchange.CollectionRequest;
import nl.geostandaarden.imx.orchestrate.engine.exchange.ObjectRequest;
import nl.geostandaarden.imx.orchestrate.engine.selection.SelectionBuilder;
import nl.geostandaarden.imx.orchestrate.model.Attribute;
import nl.geostandaarden.imx.orchestrate.model.Model;
import nl.geostandaarden.imx.orchestrate.model.ObjectType;
import nl.geostandaarden.imx.orchestrate.model.types.ScalarTypes;
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
        var selection = SelectionBuilder.newObjectNode(createModel(), "abc")
                .objectKey(Map.of("id", "123"))
                .select("attr")
                .build();

        return selection.toRequest();
    }

    private CollectionRequest getCollectionRequest() {
        var selection = SelectionBuilder.newCollectionNode(createModel(), "abc")
                .select("attr")
                .build();

        return selection.toRequest();
    }

    private BatchRequest getBatchRequest() {
        var selection = SelectionBuilder.newBatchNode(createModel(), "abc")
                .objectKey(Map.of("id", "123"))
                .objectKey(Map.of("id", "456"))
                .select("attr")
                .build();

        return selection.toRequest();
    }

    private Model createModel() {
        return Model.builder()
                .objectType(ObjectType.builder()
                        .name("abc")
                        .property(Attribute.builder()
                                .name("attr")
                                .type(ScalarTypes.STRING)
                                .build())
                        .build())
                .build();
    }
}
