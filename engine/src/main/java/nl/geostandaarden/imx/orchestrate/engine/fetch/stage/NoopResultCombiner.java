package nl.geostandaarden.imx.orchestrate.engine.fetch.stage;

import nl.geostandaarden.imx.orchestrate.engine.exchange.BatchResult;
import nl.geostandaarden.imx.orchestrate.engine.exchange.CollectionResult;
import nl.geostandaarden.imx.orchestrate.engine.exchange.DataResult;
import nl.geostandaarden.imx.orchestrate.engine.exchange.ObjectResult;

public class NoopResultCombiner implements NextResultCombiner {

    @Override
    public ObjectResult combine(ObjectResult result, DataResult nextResult) {
        return (ObjectResult) nextResult;
    }

    @Override
    public CollectionResult combine(CollectionResult result, DataResult nextResult) {
        return (CollectionResult) nextResult;
    }

    @Override
    public BatchResult combine(BatchResult result, DataResult nextResult) {
        return (BatchResult) nextResult;
    }
}
