package nl.geostandaarden.imx.orchestrate.engine.selection;

import java.util.Collections;
import java.util.Map;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import nl.geostandaarden.imx.orchestrate.model.Attribute;

@Getter
@ToString
@SuperBuilder(toBuilder = true)
public final class AttributeNode implements TreeNode {

    private final Attribute attribute;

    @Override
    public Map<String, TreeNode> getChildNodes() {
        return Collections.emptyMap();
    }

    public static AttributeNode forAttribute(Attribute attribute) {
        return AttributeNode.builder() //
                .attribute(attribute)
                .build();
    }
}
