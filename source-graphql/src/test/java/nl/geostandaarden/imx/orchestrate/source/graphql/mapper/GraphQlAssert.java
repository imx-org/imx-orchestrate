package nl.geostandaarden.imx.orchestrate.source.graphql.mapper;

import graphql.language.Argument;
import graphql.language.Document;
import graphql.language.Field;
import graphql.language.OperationDefinition;
import graphql.parser.InvalidSyntaxException;
import graphql.parser.Parser;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;
import org.assertj.core.api.AbstractAssert;

public class GraphQlAssert extends AbstractAssert<GraphQlAssert, String> {

  @Data
  @Builder
  private static class QueryNode {
    private String name;
    private List<Argument> arguments;

    @Singular("node")
    private List<QueryNode> nodes;
  }

  public GraphQlAssert(String actual) {
    super(actual, GraphQlAssert.class);
  }

  public static GraphQlAssert assertThat(String actual) {
    return new GraphQlAssert(actual);
  }

  public GraphQlAssert graphQlEquals(String query) {
    isNotNull();
    compareQueryNodes(getQueryNodes(actual), getQueryNodes(query), "");
    return this;
  }

  private void compareQueryNodes(List<GraphQlAssert.QueryNode> actualNodes, List<GraphQlAssert.QueryNode> expectedNodes, String field) {
    if (expectedNodes.size() != actualNodes.size()) {
      failWithMessage("Expected %s to have %d fields, but actually found %d.", field, expectedNodes.size(), actualNodes.size());
    }

    var iteratorActual = actualNodes.iterator();
    var iteratorExpected = expectedNodes.iterator();

    while (iteratorActual.hasNext() && iteratorExpected.hasNext()) {
      var queryNodeActual = iteratorActual.next();
      var queryNodeExpected = iteratorExpected.next();

      if (!queryNodeActual.getName().equals(queryNodeExpected.getName())) {
        failWithMessage("Expected field %s to have field %s, but actually found %s.", field, queryNodeExpected.getName(), queryNodeActual.getName());
      }

      compareArguments(queryNodeActual, queryNodeExpected);

      compareQueryNodes(queryNodeActual.getNodes(), queryNodeExpected.getNodes(), queryNodeExpected.getName());
    }
  }

  private void compareArguments(QueryNode queryNodeActual, QueryNode queryNodeExpected) {
    if (queryNodeExpected.getArguments().size() != queryNodeActual.getArguments().size()) {
      failWithMessage("Expected field %s to have %d arguments, but actually found %d.", queryNodeExpected.getName(), queryNodeExpected.getArguments().size(), queryNodeActual.getArguments().size());
    }

    var iteratorExpected = queryNodeExpected.getArguments().iterator();
    var iteratorActual = queryNodeActual.getArguments().iterator();

    while (iteratorActual.hasNext() && iteratorExpected.hasNext()) {
      var argumentActual = iteratorActual.next();
      var argumentExpected = iteratorExpected.next();

      compareArgument(argumentActual, argumentExpected, queryNodeActual.getName());
    }

  }

  private void compareArgument(Argument argumentActual, Argument argumentExpected, String field) {
    if (!argumentActual.getName().equals(argumentExpected.getName())) {
      failWithMessage("Expected field %s to have argument with name %s, but actually found %s.", field, argumentExpected.getName(), argumentActual.getName());
    }

    //TODO Create better comparison for comparing argument values, especially regarding filters.
    if (!argumentActual.getValue().toString().equals(argumentExpected.getValue().toString())) {
      failWithMessage("Expected field %s to have argument with value %s, but actually found %s.", field, argumentExpected.getValue(), argumentActual.getValue());
    }

  }

  private List<GraphQlAssert.QueryNode> getQueryNodes(String query) {
    Document document = null;
    try {
      document = Parser.parse(query);
    } catch (InvalidSyntaxException e) {
      failWithMessage("Invalid graphQL syntax.");
    }

    var selectionSet = document.getDefinitionsOfType(OperationDefinition.class).get(0).getSelectionSet();
    var selections = selectionSet.getSelections();

    var nodeList = new ArrayList<GraphQlAssert.QueryNode>();

    selections.forEach(item -> {
      if (item instanceof Field field) {
        var arguments = new ArrayList<>(field.getArguments());
        arguments.sort(Comparator.comparing(Argument::getName));

        nodeList.add(GraphQlAssert.QueryNode.builder()
          .name(field.getName())
          .arguments(arguments)
          .nodes(getChildren(field))
          .build());
      }
    });

    nodeList.sort(Comparator.comparing(GraphQlAssert.QueryNode::getName));

    return nodeList;
  }

  private List<GraphQlAssert.QueryNode> getChildren(Field field) {
    var children = new ArrayList<GraphQlAssert.QueryNode>();

    if (field.getSelectionSet() != null) {
      field.getSelectionSet().getSelections().forEach(item -> {
        if (item instanceof Field child) {
          var arguments = new ArrayList<>(child.getArguments());
          arguments.sort(Comparator.comparing(Argument::getName));

          children.add(GraphQlAssert.QueryNode.builder()
            .name(child.getName())
            .arguments(arguments)
            .nodes(getChildren(child))
            .build());
        }
      });
    }

    children.sort(Comparator.comparing(GraphQlAssert.QueryNode::getName));

    return children;
  }

}
