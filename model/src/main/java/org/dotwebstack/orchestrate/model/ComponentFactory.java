package org.dotwebstack.orchestrate.model;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.dotwebstack.orchestrate.model.combiners.Coalesce;
import org.dotwebstack.orchestrate.model.combiners.Join;
import org.dotwebstack.orchestrate.model.combiners.ResultCombiner;
import org.dotwebstack.orchestrate.model.combiners.ResultCombinerType;
import org.dotwebstack.orchestrate.model.mappers.Append;
import org.dotwebstack.orchestrate.model.mappers.Cel;
import org.dotwebstack.orchestrate.model.mappers.Prepend;
import org.dotwebstack.orchestrate.model.mappers.ResultMapper;
import org.dotwebstack.orchestrate.model.mappers.ResultMapperType;
import org.dotwebstack.orchestrate.model.mappers.ToString;
import org.dotwebstack.orchestrate.model.matchers.IsNull;
import org.dotwebstack.orchestrate.model.matchers.NotNull;
import org.dotwebstack.orchestrate.model.matchers.ResultMatcher;
import org.dotwebstack.orchestrate.model.matchers.ResultMatcherType;

public final class ComponentFactory {

  private final Map<String, ResultMapperType> resultMapperTypes = new HashMap<>();

  private final Map<String, ResultMatcherType> resultMatcherTypes = new HashMap<>();

  private final Map<String, ResultCombinerType> resultCombinerTypes = new HashMap<>();

  public ComponentFactory() {
    register(new Append(), new Prepend(), new ToString(), new Cel());
    register(new NotNull(), new IsNull());
    register(new Coalesce(), new Join());
  }

  public ComponentFactory register(ResultMapperType... resultMapperTypes) {
    Arrays.stream(resultMapperTypes).forEach(resultMapperType ->
        this.resultMapperTypes.put(resultMapperType.getName(), resultMapperType));
    return this;
  }

  public ComponentFactory register(ResultMatcherType... resultMatcherTypes) {
    Arrays.stream(resultMatcherTypes).forEach(resultMatcherType ->
        this.resultMatcherTypes.put(resultMatcherType.getName(), resultMatcherType));
    return this;
  }

  public ComponentFactory register(ResultCombinerType... resultCombinerTypes) {
    Arrays.stream(resultCombinerTypes).forEach(resultCombinerType ->
        this.resultCombinerTypes.put(resultCombinerType.getName(), resultCombinerType));
    return this;
  }

  public ResultMapper createResultMapper(String type) {
    return createResultMapper(type, Map.of());
  }

  public ResultMapper createResultMapper(String type, Map<String, Object> options) {
    return resultMapperTypes.get(type)
        .create(options);
  }

  public ResultMatcher createResultMatcher(String type) {
    return createResultMatcher(type, Map.of());
  }

  public ResultMatcher createResultMatcher(String type, Map<String, Object> options) {
    return resultMatcherTypes.get(type)
        .create(options);
  }

  public ResultCombiner createResultCombiner(String type) {
    return createResultCombiner(type, Map.of());
  }

  public ResultCombiner createResultCombiner(String type, Map<String, Object> options) {
    return resultCombinerTypes.get(type)
        .create(options);
  }
}
