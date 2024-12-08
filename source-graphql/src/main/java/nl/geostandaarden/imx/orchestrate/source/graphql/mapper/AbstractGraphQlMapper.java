package nl.geostandaarden.imx.orchestrate.source.graphql.mapper;

import graphql.ExecutionInput;
import graphql.language.Field;
import graphql.language.SelectionSet;
import java.util.List;
import nl.geostandaarden.imx.orchestrate.engine.exchange.DataRequest;
import nl.geostandaarden.imx.orchestrate.engine.selection.CompoundNode;
import nl.geostandaarden.imx.orchestrate.engine.selection.TreeNode;
import nl.geostandaarden.imx.orchestrate.engine.source.SourceException;

abstract class AbstractGraphQlMapper<T extends DataRequest<?>> {

    abstract ExecutionInput convert(T request);

    protected SelectionSet createSelectionSet(CompoundNode selection) {
        if (selection.getChildNodes().isEmpty()) {
            throw new SourceException("SelectionSet cannot be empty.");
        }

        var fields = selection.getChildNodes().entrySet().stream()
                .map(entry -> getField(entry.getKey(), entry.getValue()))
                .toList();

        return new SelectionSet(fields);
    }

    private Field getField(String name, TreeNode node) {
        SelectionSet selectionSet = null;

        if (node instanceof CompoundNode compoundNode) {
            selectionSet = createSelectionSet(compoundNode);
        }

        return new Field(name, List.of(), selectionSet);
    }
}
