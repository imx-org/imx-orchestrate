package nl.geostandaarden.imx.orchestrate.engine.selection;

import java.util.Map;
import lombok.Getter;
import lombok.Singular;
import lombok.experimental.SuperBuilder;
import nl.geostandaarden.imx.orchestrate.engine.source.Source;
import nl.geostandaarden.imx.orchestrate.model.ObjectType;

@Getter
@SuperBuilder(toBuilder = true)
abstract class CompoundNode implements TreeNode {

    @Singular
    private final Map<String, TreeNode> childNodes;

    private final ObjectType objectType;

    private final Source source;

    @Override
    public Map<String, TreeNode> getChildNodes() {
        return childNodes;
    }
}
