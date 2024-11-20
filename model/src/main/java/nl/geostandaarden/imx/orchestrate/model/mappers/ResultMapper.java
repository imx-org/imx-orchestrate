package nl.geostandaarden.imx.orchestrate.model.mappers;

import java.util.function.BiFunction;
import nl.geostandaarden.imx.orchestrate.model.Property;
import nl.geostandaarden.imx.orchestrate.model.result.PathResult;

public interface ResultMapper extends BiFunction<PathResult, Property, PathResult> {}
