package org.dotwebstack.orchestrate.model.combiners;

import java.util.List;
import java.util.function.Function;
import org.dotwebstack.orchestrate.model.PathResult;
import org.dotwebstack.orchestrate.model.PropertyResult;

public interface ResultCombiner extends Function<List<PathResult>, PropertyResult> {}
