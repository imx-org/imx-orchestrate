package nl.geostandaarden.imx.orchestrate.engine.fetch.stage;

import nl.geostandaarden.imx.orchestrate.engine.OrchestrateException;
import nl.geostandaarden.imx.orchestrate.engine.exchange.CollectionResult;
import nl.geostandaarden.imx.orchestrate.engine.exchange.DataResult;
import nl.geostandaarden.imx.orchestrate.engine.exchange.ObjectResult;

public interface NextResultCombiner {

    default ObjectResult combine(ObjectResult result, DataResult nextResult) {
        throw new OrchestrateException("ObjectResult combiner not implemented.");
    }

    default CollectionResult combine(CollectionResult result, DataResult nextResult) {
        throw new OrchestrateException("CollectionResult combiner not implemented.");
    }
}
