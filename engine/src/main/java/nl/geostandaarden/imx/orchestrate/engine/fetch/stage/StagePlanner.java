package nl.geostandaarden.imx.orchestrate.engine.fetch.stage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import nl.geostandaarden.imx.orchestrate.engine.OrchestrateException;
import nl.geostandaarden.imx.orchestrate.engine.selection.AttributeNode;
import nl.geostandaarden.imx.orchestrate.engine.selection.BatchNode;
import nl.geostandaarden.imx.orchestrate.engine.selection.CollectionNode;
import nl.geostandaarden.imx.orchestrate.engine.selection.CompoundNode;
import nl.geostandaarden.imx.orchestrate.engine.selection.ObjectNode;
import nl.geostandaarden.imx.orchestrate.engine.selection.TreeNode;
import nl.geostandaarden.imx.orchestrate.model.InverseRelation;
import nl.geostandaarden.imx.orchestrate.model.Path;
import nl.geostandaarden.imx.orchestrate.model.Relation;

public final class StagePlanner {

    private final List<ResultKeyStageCreator> nextStageCreators = new ArrayList<>();

    public Stage plan(ObjectNode selection) {
        return plan(selection, null);
    }

    public Stage plan(ObjectNode selection, NextResultCombiner resultCombiners) {
        var subSelection = createSubSelection(selection, Path.empty());

        return Stage.builder()
                .selection(subSelection)
                .nextStageCreators(Collections.unmodifiableList(nextStageCreators))
                .nextResultCombiner(resultCombiners)
                .build();
    }

    public Stage plan(CollectionNode selection) {
        return plan(selection, null);
    }

    public Stage plan(CollectionNode selection, NextResultCombiner resultCombiners) {
        var subSelection = createSubSelection(selection, Path.empty());

        return Stage.builder()
                .selection(subSelection)
                .nextStageCreators(Collections.unmodifiableList(nextStageCreators))
                .nextResultCombiner(resultCombiners)
                .build();
    }

    public Stage plan(BatchNode selection) {
        return plan(selection, null);
    }

    public Stage plan(BatchNode selection, NextResultCombiner resultCombiners) {
        var subSelection = createSubSelection(selection, Path.empty());

        return Stage.builder()
                .selection(subSelection)
                .nextStageCreators(Collections.unmodifiableList(nextStageCreators))
                .nextResultCombiner(resultCombiners)
                .build();
    }

    private ObjectNode createSubSelection(ObjectNode selection, Path path) {
        var childNodes = selectNodes(selection, path);

        return selection.toBuilder() //
                .clearChildNodes()
                .childNodes(childNodes)
                .build();
    }

    private CollectionNode createSubSelection(CollectionNode selection, Path path) {
        var childNodes = selectNodes(selection, path);

        return selection.toBuilder() //
                .clearChildNodes()
                .childNodes(childNodes)
                .build();
    }

    private BatchNode createSubSelection(BatchNode selection, Path path) {
        var childNodes = selectNodes(selection, path);

        return selection.toBuilder() //
                .clearChildNodes()
                .childNodes(childNodes)
                .build();
    }

    private Map<String, TreeNode> selectNodes(CompoundNode selection, Path path) {
        var objectType = selection.getObjectType();
        var selectedNodes = new LinkedHashMap<String, TreeNode>();

        selection.getChildNodes().forEach((name, childNode) -> {
            if (childNode instanceof CompoundNode compoundNode) {
                var childType = compoundNode.getObjectType();
                var childPath = path.append(name);

                if (compoundNode.getRelation() instanceof Relation) {
                    if (!childType.hasIdentityProperties()) {
                        if (compoundNode instanceof ObjectNode objectNode) {
                            selectedNodes.put(name, createSubSelection(objectNode, childPath));
                        } else if (compoundNode instanceof CollectionNode collectionNode) {
                            selectedNodes.put(name, createSubSelection(collectionNode, childPath));
                        } else {
                            throw new OrchestrateException("Could not process tree node: " + compoundNode);
                        }
                    } else {
                        // TODO: Make sure foreign key is selected + handle inverse relations
                        nextStageCreators.add(ResultKeyStageCreator.builder()
                                .resultPath(childPath)
                                .selection(compoundNode)
                                .build());
                    }
                } else if (compoundNode.getRelation() instanceof InverseRelation) {
                    objectType
                            .getIdentityProperties()
                            .forEach(property -> selectedNodes.put(
                                    property.getName(),
                                    AttributeNode.forAttribute(childType.getAttribute(property.getName()))));

                    nextStageCreators.add(ResultKeyStageCreator.builder()
                            .resultPath(childPath)
                            .selection(compoundNode)
                            .build());
                } else {
                    throw new OrchestrateException("Could not select path: " + path);
                }
            } else {
                selectedNodes.put(name, childNode);
            }
        });

        return Collections.unmodifiableMap(selectedNodes);
    }
}
