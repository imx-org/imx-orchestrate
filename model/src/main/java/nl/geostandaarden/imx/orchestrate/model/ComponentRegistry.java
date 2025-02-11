package nl.geostandaarden.imx.orchestrate.model;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import nl.geostandaarden.imx.orchestrate.model.combiners.CoalesceCombinerType;
import nl.geostandaarden.imx.orchestrate.model.combiners.JoinCombinerType;
import nl.geostandaarden.imx.orchestrate.model.combiners.MergeCombinerType;
import nl.geostandaarden.imx.orchestrate.model.combiners.NoopCombinerType;
import nl.geostandaarden.imx.orchestrate.model.combiners.ResultCombiner;
import nl.geostandaarden.imx.orchestrate.model.combiners.ResultCombinerType;
import nl.geostandaarden.imx.orchestrate.model.combiners.SumCombinerType;
import nl.geostandaarden.imx.orchestrate.model.mappers.AgeMapperType;
import nl.geostandaarden.imx.orchestrate.model.mappers.AppendMapperType;
import nl.geostandaarden.imx.orchestrate.model.mappers.CelMapperType;
import nl.geostandaarden.imx.orchestrate.model.mappers.DivideMapperType;
import nl.geostandaarden.imx.orchestrate.model.mappers.IsEmptyMapperType;
import nl.geostandaarden.imx.orchestrate.model.mappers.IsNotEmptyMapperType;
import nl.geostandaarden.imx.orchestrate.model.mappers.PrependMapperType;
import nl.geostandaarden.imx.orchestrate.model.mappers.ResultMapper;
import nl.geostandaarden.imx.orchestrate.model.mappers.ResultMapperType;
import nl.geostandaarden.imx.orchestrate.model.matchers.CelMatcherType;
import nl.geostandaarden.imx.orchestrate.model.matchers.EqualsMatcherType;
import nl.geostandaarden.imx.orchestrate.model.matchers.IsNullMatcherType;
import nl.geostandaarden.imx.orchestrate.model.matchers.Matcher;
import nl.geostandaarden.imx.orchestrate.model.matchers.MatcherType;
import nl.geostandaarden.imx.orchestrate.model.matchers.NotEqualsMatcherType;
import nl.geostandaarden.imx.orchestrate.model.matchers.NotNullMatcherType;

public final class ComponentRegistry {

    private final Map<String, ResultMapperType> resultMapperTypes = new HashMap<>();

    private final Map<String, ResultCombinerType> resultCombinerTypes = new HashMap<>();

    private final Map<String, MatcherType> matcherTypes = new HashMap<>();

    public ComponentRegistry() {
        register(
                new AppendMapperType(),
                new AgeMapperType(),
                new CelMapperType(),
                new DivideMapperType(),
                new IsEmptyMapperType(),
                new IsNotEmptyMapperType(),
                new PrependMapperType());
        register(
                new CoalesceCombinerType(),
                new JoinCombinerType(),
                new MergeCombinerType(),
                new NoopCombinerType(),
                new SumCombinerType());
        register(
                new CelMatcherType(),
                new EqualsMatcherType(),
                new IsNullMatcherType(),
                new NotEqualsMatcherType(),
                new NotNullMatcherType());
    }

    public ComponentRegistry register(ResultMapperType... resultMapperTypes) {
        Arrays.stream(resultMapperTypes)
                .forEach(resultMapperType -> this.resultMapperTypes.put(resultMapperType.getName(), resultMapperType));
        return this;
    }

    public ComponentRegistry register(ResultCombinerType... resultCombinerTypes) {
        Arrays.stream(resultCombinerTypes)
                .forEach(resultCombinerType ->
                        this.resultCombinerTypes.put(resultCombinerType.getName(), resultCombinerType));
        return this;
    }

    public ComponentRegistry register(MatcherType... matcherTypes) {
        Arrays.stream(matcherTypes).forEach(matcherType -> this.matcherTypes.put(matcherType.getName(), matcherType));
        return this;
    }

    public ResultMapper createResultMapper(String type) {
        return createResultMapper(type, Map.of());
    }

    public ResultMapper createResultMapper(String type, Map<String, Object> options) {
        try {
            return resultMapperTypes.get(type).create(options);
        } catch (NullPointerException e) {
            throw new ModelException("Unknown result mapper: " + type, e);
        }
    }

    public ResultCombiner createResultCombiner(String type) {
        return createResultCombiner(type, Map.of());
    }

    public ResultCombiner createResultCombiner(String type, Map<String, Object> options) {
        try {
            return resultCombinerTypes.get(type).create(options);
        } catch (NullPointerException e) {
            throw new ModelException("Unknown result combiner: " + type, e);
        }
    }

    public Matcher createMatcher(String type) {
        return createMatcher(type, Map.of());
    }

    public Matcher createMatcher(String type, Map<String, Object> options) {
        try {
            return matcherTypes.get(type).create(options);
        } catch (NullPointerException e) {
            throw new ModelException("Unknown matcher: " + type, e);
        }
    }
}
