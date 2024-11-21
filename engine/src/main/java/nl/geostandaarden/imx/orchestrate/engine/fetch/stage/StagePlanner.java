package nl.geostandaarden.imx.orchestrate.engine.fetch.stage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import nl.geostandaarden.imx.orchestrate.engine.exchange.CollectionRequest;
import nl.geostandaarden.imx.orchestrate.engine.exchange.ObjectRequest;
import nl.geostandaarden.imx.orchestrate.engine.selection.CollectionNode;
import nl.geostandaarden.imx.orchestrate.engine.selection.ObjectNode;
import nl.geostandaarden.imx.orchestrate.engine.selection.TreeNode;
import nl.geostandaarden.imx.orchestrate.model.Path;

public class StagePlanner {

    private final List<ResultPathStageCreator> nextStageCreators = new ArrayList<>();

    public Stage plan(ObjectNode selection) {
        var subSelection = createSubSelection(selection, Path.empty());

        var request = ObjectRequest.builder(null).selection(subSelection).build();

        return Stage.builder()
                .request(request)
                .nextStageCreators(Collections.unmodifiableList(nextStageCreators))
                .build();
    }

    public Stage plan(CollectionNode selection) {
        var subSelection = createSubSelection(selection, Path.empty());

        var request = CollectionRequest.builder(null).selection(subSelection).build();

        return Stage.builder()
                .request(request)
                .nextStageCreators(Collections.unmodifiableList(nextStageCreators))
                .build();
    }

    private ObjectNode createSubSelection(ObjectNode selection, Path path) {
        var childNodes = createChildNodes(selection, path);

        return selection.toBuilder().clearChildNodes().childNodes(childNodes).build();
    }

    private CollectionNode createSubSelection(CollectionNode selection, Path path) {
        var childNodes = createChildNodes(selection, path);

        return selection.toBuilder().clearChildNodes().childNodes(childNodes).build();
    }

    private Map<String, TreeNode> createChildNodes(TreeNode selection, Path path) {
        var childNodes = new LinkedHashMap<String, TreeNode>();

        selection.getChildNodes().forEach((name, childNode) -> {
            if (childNode instanceof ObjectNode objectNode) {
                var childType = objectNode.getObjectType();
                var childPath = path.append(name);

                if (!childType.hasIdentityProperties()) {
                    childNodes.put(name, createSubSelection(objectNode, childPath));
                } else {
                    // TODO: Make sure foreign key is selected + handle inverse relations
                    nextStageCreators.add(ResultPathStageCreator.builder()
                            .stagePlanner(this)
                            .resultPath(childPath)
                            .selection(objectNode)
                            .build());
                }
            } else {
                childNodes.put(name, childNode);
            }
        });

        return Collections.unmodifiableMap(childNodes);
    }
}
