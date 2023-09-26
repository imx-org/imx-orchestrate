package nl.geostandaarden.imx.orchestrate.gateway.fetch;

import static graphql.schema.GraphQLTypeUtil.unwrapAll;
import static graphql.schema.GraphQLTypeUtil.unwrapNonNull;
import static java.util.function.Predicate.not;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.DataFetchingFieldSelectionSet;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLScalarType;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import nl.geostandaarden.imx.orchestrate.engine.OrchestrateEngine;
import nl.geostandaarden.imx.orchestrate.engine.OrchestrateException;
import nl.geostandaarden.imx.orchestrate.engine.exchange.AbstractDataRequest;
import nl.geostandaarden.imx.orchestrate.engine.exchange.CollectionRequest;
import nl.geostandaarden.imx.orchestrate.engine.exchange.DataRequest;
import nl.geostandaarden.imx.orchestrate.engine.exchange.ObjectRequest;
import nl.geostandaarden.imx.orchestrate.engine.exchange.ObjectResult;
import nl.geostandaarden.imx.orchestrate.gateway.schema.SchemaConstants;
import nl.geostandaarden.imx.orchestrate.gateway.schema.SchemaUtils;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

@RequiredArgsConstructor
public final class GenericDataFetcher implements DataFetcher<Publisher<Map<String, Object>>> {

  private final OrchestrateEngine engine;

  @Override
  public Publisher<Map<String, Object>> get(DataFetchingEnvironment environment) {
    var request = createRequest(environment);

    if (request instanceof ObjectRequest objectRequest) {
      return engine.fetch(objectRequest)
          .map(ObjectResult::getProperties);
    }

    if (request instanceof CollectionRequest collectionRequest) {
      return engine.fetch(collectionRequest)
          .flatMapMany(result -> Flux.fromIterable(result.getObjectResults()))
          .map(ObjectResult::getProperties);
    }

    throw new OrchestrateException("Unsupported request: " + request.getClass());
  }

  private DataRequest createRequest(DataFetchingEnvironment environment) {
    var fieldName = environment.getField()
        .getName();
    var fieldTypeName = unwrapAll(environment.getFieldType())
        .getName();
    var targetModel = engine.getModelMapping()
        .getTargetModel();

    if (fieldName.endsWith(SchemaConstants.QUERY_COLLECTION_SUFFIX)) {
      var requestBuilder = CollectionRequest.builder(targetModel)
          .objectType(fieldTypeName);

      return selectProperties(requestBuilder, environment.getSelectionSet())
          .build();
    }

    var requestBuilder = ObjectRequest.builder(targetModel)
        .objectType(fieldTypeName)
        .objectKey(environment.getArguments());

    return selectProperties(requestBuilder, environment.getSelectionSet())
        .build();
  }

  private <B extends AbstractDataRequest.Builder<B>> B selectProperties(B requestBuilder, DataFetchingFieldSelectionSet selectionSet) {
    selectionSet.getImmediateFields()
        .stream()
        .filter(not(selectedField -> SchemaUtils.isReservedField(selectedField, null)))
        .forEach(selectedField -> {
          var fieldName = selectedField.getName();
          var fieldType = unwrapNonNull(selectedField.getType());

          if (fieldType instanceof GraphQLObjectType) {
            requestBuilder.selectObjectProperty(fieldName, nestedRequestBuilder -> {
              selectProperties(nestedRequestBuilder, selectedField.getSelectionSet());
              return nestedRequestBuilder.build();
            });

            return;
          }

          if (fieldType instanceof GraphQLList) {
            requestBuilder.selectCollectionProperty(fieldName, nestedRequestBuilder -> {
              selectProperties(nestedRequestBuilder, selectedField.getSelectionSet());
              return nestedRequestBuilder.build();
            });

            return;
          }

          if (fieldType instanceof GraphQLScalarType) {
            requestBuilder.selectProperty(fieldName);
            return;
          }

          throw new UnsupportedOperationException();
        });

    return requestBuilder;
  }
}
