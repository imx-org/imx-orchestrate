package nl.geostandaarden.imx.orchestrate.source.graphql.mapper;

import static org.springframework.util.StringUtils.uncapitalize;

import graphql.ExecutionInput;
import graphql.language.Argument;
import graphql.language.AstPrinter;
import graphql.language.Field;
import graphql.language.OperationDefinition;
import graphql.language.SelectionSet;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import nl.geostandaarden.imx.orchestrate.engine.exchange.BatchRequest;
import nl.geostandaarden.imx.orchestrate.engine.source.SourceException;
import nl.geostandaarden.imx.orchestrate.source.graphql.config.GraphQlOrchestrateConfig;

@RequiredArgsConstructor
public class BatchGraphQlMapper extends AbstractGraphQlMapper<BatchRequest> {

  private static final String OPERATION_NAME = "Query";

  private final GraphQlOrchestrateConfig config;

  public ExecutionInput convert(BatchRequest request) {
    var fieldName = uncapitalize(request.getObjectType()
        .getName()) + config.getBatchSuffix();

    var arguments = getArguments(request);

    var selectionSet = createSelectionSet(request.getSelectedProperties());
    var queryField = new Field(fieldName, arguments, selectionSet);

    var query = OperationDefinition.newOperationDefinition()
        .name(OPERATION_NAME)
        .operation(OperationDefinition.Operation.QUERY)
        .selectionSet(new SelectionSet(List.of(queryField)))
        .build();

    return ExecutionInput.newExecutionInput()
        .query(AstPrinter.printAst(query))
        .build();
  }

  private List<Argument> getArguments(BatchRequest request) {
    var argumentMap = new ConcurrentHashMap<String, List<String>>();

    for (var objectKey : request.getObjectKeys()) {
      var entry = objectKey.entrySet()
          .stream()
          .findFirst()
          .orElseThrow();

      if (!argumentMap.containsKey(entry.getKey())) {
        argumentMap.put(entry.getKey(), new ArrayList<>());
      }

      argumentMap.get(entry.getKey())
          .add((String) entry.getValue());
    }

    if (argumentMap.size() > 1) {
      throw new SourceException("Batch requests can only contain values for 1 key property.");
    }

    var argument = argumentMap.entrySet()
        .stream()
        .findFirst()
        .orElseThrow();
    return List.of(getArgument(argument.getKey(), argument.getValue()));
  }

  private Argument getArgument(String name, Object value) {
    return new Argument(name, ValueMapper.mapToValue(value));
  }

}
