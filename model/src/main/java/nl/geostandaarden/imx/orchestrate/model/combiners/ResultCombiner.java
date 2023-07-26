package nl.geostandaarden.imx.orchestrate.model.combiners;

import java.util.List;
import java.util.function.Function;
import nl.geostandaarden.imx.orchestrate.model.PathResult;
import nl.geostandaarden.imx.orchestrate.model.PropertyResult;

public interface ResultCombiner extends Function<List<PathResult>, PropertyResult> {}
