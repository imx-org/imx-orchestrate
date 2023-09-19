package nl.geostandaarden.imx.orchestrate.engine.fetch;

import static graphql.schema.GraphQLTypeUtil.unwrapAll;
import static graphql.schema.GraphQLTypeUtil.unwrapNonNull;

import graphql.schema.*;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import nl.geostandaarden.imx.orchestrate.engine.exchange.AbstractDataRequest;
import nl.geostandaarden.imx.orchestrate.engine.exchange.CollectionRequest;
import nl.geostandaarden.imx.orchestrate.engine.exchange.DataRequest;
import nl.geostandaarden.imx.orchestrate.engine.exchange.ObjectRequest;
import nl.geostandaarden.imx.orchestrate.engine.schema.SchemaConstants;
import nl.geostandaarden.imx.orchestrate.model.Model;
import org.reactivestreams.Publisher;

@RequiredArgsConstructor
public final class GenericDataFetcher implements DataFetcher<Publisher<Map<String, Object>>> {

  private final FetchPlanner fetchPlanner;

  private final Model model;

  @Override
  public Publisher<Map<String, Object>> get(DataFetchingEnvironment environment) {
    return fetchPlanner.fetch(createRequest(environment), environment.getArguments());
  }

  private DataRequest createRequest(DataFetchingEnvironment environment) {
    var typeName = unwrapAll(environment.getFieldType()).getName();

    if (typeName.endsWith(SchemaConstants.QUERY_COLLECTION_SUFFIX)) {
      var requestBuilder = CollectionRequest.builder(model)
          .objectType(typeName);

      return selectProperties(requestBuilder, environment.getSelectionSet())
          .build();
    }

    var requestBuilder = ObjectRequest.builder(model)
        .objectType(typeName);

    return selectProperties(requestBuilder, environment.getSelectionSet())
        .build();
  }

  private <B extends AbstractDataRequest.Builder<B>> B selectProperties(B requestBuilder, DataFetchingFieldSelectionSet selectionSet) {
    selectionSet.getImmediateFields()
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
