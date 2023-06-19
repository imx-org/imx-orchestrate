package nl.kadaster.gdc.orchestrate.mapper;

import graphql.language.Argument;
import graphql.language.Field;
import graphql.language.OperationDefinition;
import graphql.parser.Parser;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;

public class MapperTestUtils {

  @Data
  @Builder
  private static class QueryNode {
    private String name;
    private List<Argument> arguments;

    @Singular("node")
    private List<QueryNode> nodes;
  }

  static boolean graphQlEquals(String query1, String query2) {
    var query1Nodes = getQueryNodes(query1);
    var query2Nodes = getQueryNodes(query2);

    return compareQueryNodes(query1Nodes, query2Nodes);
  }

  private static boolean compareQueryNodes(List<QueryNode> queryNodes1, List<QueryNode> queryNodes2) {
    Iterator<QueryNode> iterator1 = queryNodes1.iterator();
    Iterator<QueryNode> iterator2 = queryNodes2.iterator();

    while (iterator1.hasNext() && iterator2.hasNext()) {
      var queryNode1 = iterator1.next();
      var queryNode2 = iterator2.next();

      if (!queryNode1.getName().equals(queryNode2.getName())) {
        return false;
      }
      if (!compareArguments(queryNode1, queryNode2)) {
        return false;
      }
      if (!compareQueryNodes(queryNode1.getNodes(), queryNode2.getNodes())) {
        return false;
      }
    }

    if (iterator1.hasNext() || iterator2.hasNext()) {
      return false;
    }

    return true;
  }

  private static boolean compareArguments(QueryNode queryNode1, QueryNode queryNode2) {
    Iterator<Argument> iterator1 = queryNode1.getArguments().iterator();
    Iterator<Argument> iterator2 = queryNode2.getArguments().iterator();

    while (iterator1.hasNext() && iterator2.hasNext()) {
      var argument1 = iterator1.next();
      var argument2 = iterator2.next();

      if (!argument1.isEqualTo(argument2)) {
        return false;
      }
    }

    if (iterator1.hasNext() || iterator2.hasNext()) {
      return false;
    }

    return true;
  }

  private static List<QueryNode> getQueryNodes(String query) {
    var parser = Parser.parse(query);

    var selectionSet = parser.getDefinitionsOfType(OperationDefinition.class).get(0).getSelectionSet();
    var selections = selectionSet.getSelections();

    var nodeList = new ArrayList<QueryNode>();

    selections.forEach(item -> {
      if (item instanceof Field field) {
        var arguments = new ArrayList<>(field.getArguments());
        arguments.sort(Comparator.comparing(Argument::getName));

        nodeList.add(QueryNode.builder()
          .name(field.getName())
          .arguments(arguments)
          .nodes(getChildren(field))
          .build());
      }
    });

    nodeList.sort(Comparator.comparing(QueryNode::getName));

    return nodeList;
  }

  private static List<QueryNode> getChildren(Field field) {
    var children = new ArrayList<QueryNode>();

    if (field.getSelectionSet() != null) {
      field.getSelectionSet().getSelections().forEach(item -> {
        if (item instanceof Field child) {
          var arguments = new ArrayList<>(child.getArguments());
          arguments.sort(Comparator.comparing(Argument::getName));

          children.add(QueryNode.builder()
            .name(child.getName())
            .arguments(arguments)
            .nodes(getChildren(child))
            .build());
        }
      });
    }

    children.sort(Comparator.comparing(QueryNode::getName));

    return children;
  }

}
