package nl.geostandaarden.imx.orchestrate.engine.selection;

import java.util.Map;
import lombok.Getter;
import lombok.Singular;
import lombok.experimental.SuperBuilder;
import nl.geostandaarden.imx.orchestrate.model.AbstractRelation;
import nl.geostandaarden.imx.orchestrate.model.ObjectType;

@Getter
@SuperBuilder(toBuilder = true)
abstract class AbstractCompoundNode implements CompoundNode {

    private final AbstractRelation relation;

    @Singular
    private final Map<String, TreeNode> childNodes;

    private final ObjectType objectType;

    private final String modelAlias;

    public String toString() {
        return childNodes.entrySet().stream()
                .map(entry -> entry.getValue() instanceof CompoundNode ? entry.toString() : entry.getKey())
                .toList()
                .toString();
    }

    @Override
    public Map<String, TreeNode> getChildNodes() {
        return childNodes;
    }
}
