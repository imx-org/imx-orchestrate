package org.dotwebstack.orchestrate.model.mappers;

import java.util.function.BiFunction;
import org.dotwebstack.orchestrate.model.PathResult;
import org.dotwebstack.orchestrate.model.Property;

public interface ResultMapper extends BiFunction<PathResult, Property, PathResult> {}
