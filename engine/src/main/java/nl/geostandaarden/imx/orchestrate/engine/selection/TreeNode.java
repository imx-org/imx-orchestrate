package nl.geostandaarden.imx.orchestrate.engine.selection;

import java.util.Map;

public interface TreeNode {

    Map<String, TreeNode> getChildNodes();
}
