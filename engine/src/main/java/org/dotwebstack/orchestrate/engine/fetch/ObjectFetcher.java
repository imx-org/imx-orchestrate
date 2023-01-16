package org.dotwebstack.orchestrate.engine.fetch;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLObjectType;
import java.util.Map;
import java.util.function.UnaryOperator;
import lombok.RequiredArgsConstructor;
import org.dotwebstack.orchestrate.engine.OrchestrateException;
import org.dotwebstack.orchestrate.engine.source.ObjectRequest;
import org.dotwebstack.orchestrate.engine.source.SelectedField;
import org.dotwebstack.orchestrate.model.FieldPath;
import org.dotwebstack.orchestrate.model.ModelMapping;
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

    var typeName = ((GraphQLObjectType) fieldType).getName();
    var objectTypeMapping = modelMapping.getObjectTypeMapping(typeName)
        .orElseThrow(() -> new OrchestrateException("No mapping found for object type: " + typeName));

    var objectRequestBuilder = ObjectRequest.builder();
    var objectResultProcessorBuilder = ObjectResultProcessor.builder();

    environment.getSelectionSet()
        .getImmediateFields()
        .forEach(selectedField -> {
          var fieldName = selectedField.getName();
          var fieldMapping = objectTypeMapping.getFieldMappings()
              .get(fieldName);

          fieldMapping.getSourcePaths()
              .forEach(sourcePath -> objectRequestBuilder.selectedField(createSelectedField(sourcePath)));

          objectResultProcessorBuilder.fieldProcessor(fieldName, UnaryOperator.identity());
        });

    var objectRequest = objectRequestBuilder.build();
    var objectResultProcessor = objectResultProcessorBuilder.build();

    return Mono.just(Map.of("id", 123, "name", "Ugchelen", "municipality", "Apeldoorn", "province", "Gelderland"));
  }

  private SelectedField createSelectedField(FieldPath sourcePath) {
    return SelectedField.builder()
        .build();
  }
}
