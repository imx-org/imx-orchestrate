package nl.geostandaarden.imx.orchestrate.engine.exchange;

import nl.geostandaarden.imx.orchestrate.engine.selection.CompoundNode;

public interface DataRequest<S extends CompoundNode> {

    S getSelection();
}
