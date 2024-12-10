package nl.geostandaarden.imx.orchestrate.engine.selection;

import java.util.Map;
import nl.geostandaarden.imx.orchestrate.model.AbstractRelation;
import nl.geostandaarden.imx.orchestrate.model.ObjectType;

public interface CompoundNode extends TreeNode {

    AbstractRelation getRelation();

    Map<String, TreeNode> getChildNodes();

    ObjectType getObjectType();

    String getModelAlias();
}
