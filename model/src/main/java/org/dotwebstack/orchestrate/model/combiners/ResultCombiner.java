package org.dotwebstack.orchestrate.model.combiners;

import java.util.List;
import java.util.function.Function;

public interface ResultCombiner extends Function<List<Object>, Object> {}
