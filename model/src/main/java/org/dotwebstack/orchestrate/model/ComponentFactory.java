package org.dotwebstack.orchestrate.model;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.dotwebstack.orchestrate.model.combiners.CoalesceType;
import org.dotwebstack.orchestrate.model.combiners.JoinType;
import org.dotwebstack.orchestrate.model.combiners.NoopType;
import org.dotwebstack.orchestrate.model.combiners.ResultCombiner;
import org.dotwebstack.orchestrate.model.combiners.ResultCombinerType;
import org.dotwebstack.orchestrate.model.combiners.SumType;
import org.dotwebstack.orchestrate.model.mappers.AppendType;
import org.dotwebstack.orchestrate.model.mappers.CelType;
import org.dotwebstack.orchestrate.model.mappers.PrependType;
import org.dotwebstack.orchestrate.model.mappers.ResultMapper;
import org.dotwebstack.orchestrate.model.mappers.ResultMapperType;
import org.dotwebstack.orchestrate.model.mappers.ToStringType;
import org.dotwebstack.orchestrate.model.matchers.EqualsType;
import org.dotwebstack.orchestrate.model.matchers.IsNullType;
import org.dotwebstack.orchestrate.model.matchers.Matcher;
import org.dotwebstack.orchestrate.model.matchers.MatcherType;
import org.dotwebstack.orchestrate.model.matchers.NotEqualsType;
import org.dotwebstack.orchestrate.model.matchers.NotNullType;

public final class ComponentFactory {

  private final Map<String, ResultMapperType> resultMapperTypes = new HashMap<>();

  private final Map<String, ResultCombinerType> resultCombinerTypes = new HashMap<>();

  private final Map<String, MatcherType> matcherTypes = new HashMap<>();

  public ComponentFactory() {
    register(new AppendType(), new CelType(), new PrependType(), new ToStringType());
    register(new CoalesceType(), new JoinType(), new NoopType(), new SumType());
    register(new EqualsType(), new IsNullType(), new NotEqualsType(), new NotNullType());
  }

  public ComponentFactory register(ResultMapperType... resultMapperTypes) {
    Arrays.stream(resultMapperTypes).forEach(resultMapperType ->
        this.resultMapperTypes.put(resultMapperType.getName(), resultMapperType));
    return this;
  }

  public ComponentFactory register(ResultCombinerType... resultCombinerTypes) {
    Arrays.stream(resultCombinerTypes).forEach(resultCombinerType ->
        this.resultCombinerTypes.put(resultCombinerType.getName(), resultCombinerType));
    return this;
  }

  public ComponentFactory register(MatcherType... matcherTypes) {
    Arrays.stream(matcherTypes).forEach(matcherType ->
        this.matcherTypes.put(matcherType.getName(), matcherType));
    return this;
  }

  public ResultMapper createResultMapper(String type) {
    return createResultMapper(type, Map.of());
  }

  public ResultMapper createResultMapper(String type, Map<String, Object> options) {
    return resultMapperTypes.get(type)
        .create(options);
  }

  public ResultCombiner createResultCombiner(String type) {
    return createResultCombiner(type, Map.of());
  }

  public ResultCombiner createResultCombiner(String type, Map<String, Object> options) {
    return resultCombinerTypes.get(type)
        .create(options);
  }

  public Matcher createMatcher(String type) {
    return createMatcher(type, Map.of());
  }

  public Matcher createMatcher(String type, Map<String, Object> options) {
    return matcherTypes.get(type)
        .create(options);
  }
}
