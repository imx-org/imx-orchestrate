package org.dotwebstack.orchestrate.model;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.dotwebstack.orchestrate.model.combiners.CoalesceCombinerType;
import org.dotwebstack.orchestrate.model.combiners.JoinCombinerType;
import org.dotwebstack.orchestrate.model.combiners.MergeCombinerType;
import org.dotwebstack.orchestrate.model.combiners.NoopCombinerType;
import org.dotwebstack.orchestrate.model.combiners.ResultCombiner;
import org.dotwebstack.orchestrate.model.combiners.ResultCombinerType;
import org.dotwebstack.orchestrate.model.combiners.SumCombinerType;
import org.dotwebstack.orchestrate.model.filters.EqualsOperatorType;
import org.dotwebstack.orchestrate.model.filters.FilterOperator;
import org.dotwebstack.orchestrate.model.filters.FilterOperatorType;
import org.dotwebstack.orchestrate.model.mappers.AppendMapperType;
import org.dotwebstack.orchestrate.model.mappers.CelMapperType;
import org.dotwebstack.orchestrate.model.mappers.PrependMapperType;
import org.dotwebstack.orchestrate.model.mappers.ResultMapper;
import org.dotwebstack.orchestrate.model.mappers.ResultMapperType;
import org.dotwebstack.orchestrate.model.matchers.CelMatcherType;
import org.dotwebstack.orchestrate.model.matchers.EqualsMatcherType;
import org.dotwebstack.orchestrate.model.matchers.IsNullMatcherType;
import org.dotwebstack.orchestrate.model.matchers.Matcher;
import org.dotwebstack.orchestrate.model.matchers.MatcherType;
import org.dotwebstack.orchestrate.model.matchers.NotEqualsMatcherType;
import org.dotwebstack.orchestrate.model.matchers.NotNullMatcherType;

public final class ComponentRegistry {

  private final Map<String, ResultMapperType> resultMapperTypes = new HashMap<>();

  private final Map<String, ResultCombinerType> resultCombinerTypes = new HashMap<>();

  private final Map<String, MatcherType> matcherTypes = new HashMap<>();

  private final Map<String, FilterOperatorType> filterOperatorTypes = new HashMap<>();

  public ComponentRegistry() {
    register(new AppendMapperType(), new CelMapperType(), new PrependMapperType());
    register(new CoalesceCombinerType(), new JoinCombinerType(), new MergeCombinerType(), new NoopCombinerType(),
        new SumCombinerType());
    register(new CelMatcherType(), new EqualsMatcherType(), new IsNullMatcherType(), new NotEqualsMatcherType(),
        new NotNullMatcherType());
    register(new EqualsOperatorType());
  }

  public ComponentRegistry register(ResultMapperType... resultMapperTypes) {
    Arrays.stream(resultMapperTypes).forEach(resultMapperType ->
        this.resultMapperTypes.put(resultMapperType.getName(), resultMapperType));
    return this;
  }

  public ComponentRegistry register(ResultCombinerType... resultCombinerTypes) {
    Arrays.stream(resultCombinerTypes).forEach(resultCombinerType ->
        this.resultCombinerTypes.put(resultCombinerType.getName(), resultCombinerType));
    return this;
  }

  public ComponentRegistry register(MatcherType... matcherTypes) {
    Arrays.stream(matcherTypes).forEach(matcherType ->
        this.matcherTypes.put(matcherType.getName(), matcherType));
    return this;
  }

  public ComponentRegistry register(FilterOperatorType... filterOperatorTypes) {
    Arrays.stream(filterOperatorTypes).forEach(filterOperatorType ->
        this.filterOperatorTypes.put(filterOperatorType.getName(), filterOperatorType));
    return this;
  }

  public ResultMapper createResultMapper(String type) {
    return createResultMapper(type, Map.of());
  }

  public ResultMapper createResultMapper(String type, Map<String, Object> options) {
    try {
      return resultMapperTypes.get(type)
          .create(options);
    } catch (NullPointerException e) {
      throw new ModelException("Unknown result mapper: " + type, e);
    }
  }

  public ResultCombiner createResultCombiner(String type) {
    return createResultCombiner(type, Map.of());
  }

  public ResultCombiner createResultCombiner(String type, Map<String, Object> options) {
    try {
      return resultCombinerTypes.get(type)
          .create(options);
    } catch (NullPointerException e) {
      throw new ModelException("Unknown result combiner: " + type, e);
    }
  }

  public Matcher createMatcher(String type) {
    return createMatcher(type, Map.of());
  }

  public Matcher createMatcher(String type, Map<String, Object> options) {
    try {
      return matcherTypes.get(type)
          .create(options);
    } catch (NullPointerException e) {
      throw new ModelException("Unknown matcher: " + type, e);
    }
  }

  public FilterOperator createFilterOperator(String type) {
    return createFilterOperator(type, Map.of());
  }

  public FilterOperator createFilterOperator(String type, Map<String, Object> options) {
    try {
      return filterOperatorTypes.get(type)
          .create(options);
    } catch (NullPointerException e) {
      throw new ModelException("Unknown filter operator: " + type, e);
    }
  }
}
