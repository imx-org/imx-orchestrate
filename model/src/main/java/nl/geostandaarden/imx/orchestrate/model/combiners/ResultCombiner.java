package nl.geostandaarden.imx.orchestrate.model.combiners;

import java.util.List;
import java.util.function.Function;

import nl.geostandaarden.imx.orchestrate.model.result.PathResult;
import nl.geostandaarden.imx.orchestrate.model.result.PropertyMappingResult;

// TODO should probably be Function<List<PathMappingResult>, PropertyMappingResult>
public interface ResultCombiner extends Function<List<PathResult>, PropertyMappingResult> {}
