package nl.geostandaarden.imx.orchestrate.engine.fetch.stage;

import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import nl.geostandaarden.imx.orchestrate.engine.OrchestrateException;
import nl.geostandaarden.imx.orchestrate.engine.exchange.BatchRequest;
import nl.geostandaarden.imx.orchestrate.engine.exchange.CollectionRequest;
import nl.geostandaarden.imx.orchestrate.engine.exchange.ObjectRequest;
import nl.geostandaarden.imx.orchestrate.engine.selection.AttributeNode;
import nl.geostandaarden.imx.orchestrate.engine.selection.BatchNode;
import nl.geostandaarden.imx.orchestrate.engine.selection.CollectionNode;
import nl.geostandaarden.imx.orchestrate.engine.selection.CompoundNode;
import nl.geostandaarden.imx.orchestrate.engine.selection.ObjectNode;
import nl.geostandaarden.imx.orchestrate.engine.selection.TreeNode;
import nl.geostandaarden.imx.orchestrate.engine.selection.TreeResolver;
import nl.geostandaarden.imx.orchestrate.model.ConditionalMapping;
import nl.geostandaarden.imx.orchestrate.model.InverseRelation;
import nl.geostandaarden.imx.orchestrate.model.ObjectTypeMapping;
import nl.geostandaarden.imx.orchestrate.model.ObjectTypeRef;
import nl.geostandaarden.imx.orchestrate.model.Path;
import nl.geostandaarden.imx.orchestrate.model.Relation;

@RequiredArgsConstructor
public final class StagePlanner {

    private final TreeResolver treeResolver;

    private final List<NextStageCreator> nextStageCreators = new ArrayList<>();

    private final List<ConditionalStageCreator> conditionalStageCreators = new ArrayList<>();

    public Stage plan(ObjectRequest request, ObjectTypeMapping typeMapping) {
        var selection = request.getSelection();

        if (!typeMapping.getConditionalMappings().isEmpty()) {
            return plan(typeMapping.getSourceRoot(), selection.getObjectKey(), typeMapping.getConditionalMappings());
        }

        var subSelection = treeResolver.resolve(request.getSelection(), typeMapping);

        return plan(subSelection, null);
    }

    public Stage plan(
            ObjectTypeRef sourceRoot, Map<String, Object> objectKey, List<ConditionalMapping> conditionalMappings) {
        Set<Path> sourcePaths = new HashSet<>();

        conditionalMappings.forEach(conditionalMapping -> {
            sourcePaths.addAll(treeResolver.resolveSourcePaths(conditionalMapping));

            conditionalStageCreators.add(ConditionalStageCreator.builder() //
                    .conditionalMapping(conditionalMapping)
                    .sourceRoot(sourceRoot)
                    .objectKey(objectKey)
                    .treeResolver(treeResolver)
                    .build());
        });

        var subSelection = treeResolver.createObjectNode(sourceRoot, unmodifiableSet(sourcePaths), null, objectKey);

        return plan(subSelection, null).toBuilder() //
                .conditional(true)
                .build();
    }

    public Stage plan(ObjectNode selection, NextResultCombiner resultCombiners) {
        var subSelection = createSubSelection(selection, Path.empty());

        return Stage.builder()
                .selection(subSelection)
                .nextStageCreators(unmodifiableList(nextStageCreators))
                .conditionalStageCreators(unmodifiableList(conditionalStageCreators))
                .nextResultCombiner(resultCombiners)
                .build();
    }

    public Stage plan(CollectionRequest request, ObjectTypeMapping typeMapping) {
        var selection = treeResolver.resolve(request.getSelection(), typeMapping);
        return plan(selection, null);
    }

    public Stage plan(CollectionNode selection, NextResultCombiner resultCombiners) {
        var subSelection = createSubSelection(selection, Path.empty());

        return Stage.builder()
                .selection(subSelection)
                .nextStageCreators(unmodifiableList(nextStageCreators))
                .conditionalStageCreators(unmodifiableList(conditionalStageCreators))
                .nextResultCombiner(resultCombiners)
                .build();
    }

    public Stage plan(BatchRequest request, ObjectTypeMapping typeMapping) {
        var selection = treeResolver.resolve(request.getSelection(), typeMapping);
        return plan(selection, null);
    }

    public Stage plan(BatchNode selection, NextResultCombiner resultCombiners) {
        var subSelection = createSubSelection(selection, Path.empty());

        return Stage.builder()
                .selection(subSelection)
                .nextStageCreators(unmodifiableList(nextStageCreators))
                .conditionalStageCreators(unmodifiableList(conditionalStageCreators))
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
                                .treeResolver(treeResolver)
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
                            .treeResolver(treeResolver)
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
