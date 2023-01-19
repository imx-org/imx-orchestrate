package org.dotwebstack.orchestrate.engine.fetch;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLObjectType;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.dotwebstack.orchestrate.engine.OrchestrateException;
import org.dotwebstack.orchestrate.model.ModelMapping;
import org.dotwebstack.orchestrate.source.DataRepository;
import org.dotwebstack.orchestrate.source.ObjectRequest;
import org.dotwebstack.orchestrate.source.SelectedField;
import org.dotwebstack.orchestrate.source.Source;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class ObjectFetcher implements DataFetcher<Mono<Map<String, Object>>> {

  private final ModelMapping modelMapping;

  @Override
  public Mono<Map<String, Object>> get(DataFetchingEnvironment environment) {
    var fieldType = environment.getFieldType();

    if (!(fieldType instanceof GraphQLObjectType)) {
      throw new OrchestrateException("The object fetcher only supports object types, no unions or interfaces (yet).");
    }

    var objectRequest = createObjectRequest((GraphQLObjectType) fieldType);

    return createSource()
        .getDataRepository()
        .findOne(objectRequest);
  }

  private ObjectRequest createObjectRequest(GraphQLObjectType fieldType) {
    var typeName = fieldType.getName();
    var selection = createSelection();

    return ObjectRequest.builder()
        .objectType(modelMapping.getTargetModel()
            .getObjectType(typeName)
            .orElseThrow())
        .selection(selection)
        .build();
  }

  private List<SelectedField> createSelection() {
    return List.of();
  }

  private Source createSource() {
    return () -> (DataRepository) objectRequest -> {
      var data = Map.of("id", 123, "name", "Ugchelen", "municipality",
          Map.of("name", "Apeldoorn", "province",
              Map.of("name", "Gelderland")));

      return Mono.just(data);
    };
  }
}
