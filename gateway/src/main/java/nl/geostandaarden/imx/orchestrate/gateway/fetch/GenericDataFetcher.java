package nl.geostandaarden.imx.orchestrate.gateway.fetch;

import static graphql.schema.GraphQLTypeUtil.unwrapAll;
import static graphql.schema.GraphQLTypeUtil.unwrapNonNull;
import static java.util.function.Predicate.not;
import static nl.geostandaarden.imx.orchestrate.engine.fetch.FetchUtils.cast;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.DataFetchingFieldSelectionSet;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLScalarType;
import graphql.schema.SelectedField;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import nl.geostandaarden.imx.orchestrate.engine.OrchestrateEngine;
import nl.geostandaarden.imx.orchestrate.engine.OrchestrateException;
import nl.geostandaarden.imx.orchestrate.engine.exchange.AbstractDataRequest;
import nl.geostandaarden.imx.orchestrate.engine.exchange.BatchRequest;
import nl.geostandaarden.imx.orchestrate.engine.exchange.CollectionRequest;
import nl.geostandaarden.imx.orchestrate.engine.exchange.DataRequest;
import nl.geostandaarden.imx.orchestrate.engine.exchange.DataResult;
import nl.geostandaarden.imx.orchestrate.engine.exchange.ObjectRequest;
import nl.geostandaarden.imx.orchestrate.gateway.schema.SchemaConstants;
import nl.geostandaarden.imx.orchestrate.model.Attribute;
import nl.geostandaarden.imx.orchestrate.model.ObjectType;
import nl.geostandaarden.imx.orchestrate.model.Path;
import nl.geostandaarden.imx.orchestrate.model.filters.FilterExpression;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public final class GenericDataFetcher implements DataFetcher<Mono<? extends DataResult>> {

  private final OrchestrateEngine engine;

  private final String hasLineageFieldName;

  @Override
  public Mono<? extends DataResult> get(DataFetchingEnvironment environment) {
    var request = createRequest(environment);

    if (request instanceof ObjectRequest objectRequest) {
      return engine.fetch(objectRequest);
    }

    if (request instanceof CollectionRequest collectionRequest) {
      return engine.fetch(collectionRequest);
    }

    if (request instanceof BatchRequest batchRequest) {
      return engine.fetch(batchRequest);
    }

    throw new OrchestrateException("Unsupported request: " + request.getClass());
  }

  private boolean isReservedField(SelectedField selectedField) {
    var name = selectedField.getName();
    return hasLineageFieldName.equals(name) || name.startsWith("__");
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

      Map<String, Object> filterValue = environment.getArgument(SchemaConstants.QUERY_FILTER_ARGUMENT);

      if (filterValue != null) {
        var filter = createFilterExpression(targetModel.getObjectType(fieldTypeName), filterValue);
        requestBuilder.filter(filter);
      }

      return selectProperties(requestBuilder, environment.getSelectionSet())
          .build();
    }

    if (fieldName.endsWith(SchemaConstants.QUERY_BATCH_SUFFIX)) {
      List<Map<String, Object>> objectKeys = cast(environment.getArguments()
          .get(SchemaConstants.BATCH_KEYS_ARG));

      var requestBuilder = BatchRequest.builder(targetModel)
          .objectType(fieldTypeName)
          .objectKeys(objectKeys);

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
        .filter(not(this::isReservedField))
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

  private FilterExpression createFilterExpression(ObjectType targetType, Map<String, Object> arguments) {
    if (arguments.size() > 1) {
      throw new OrchestrateException("Currently only a single filter property is supported.");
    }

    var firstEntry = arguments.entrySet()
        .iterator()
        .next();

    var propertyName = firstEntry.getKey();
    var property = targetType.getProperty(propertyName);

    if (property instanceof Attribute attribute) {
      return attribute.getType()
          .createFilterExpression(Path.fromProperties(property), arguments.get(propertyName));
    }

    throw new OrchestrateException("Currently only attributes can be filtered.");
  }
}
