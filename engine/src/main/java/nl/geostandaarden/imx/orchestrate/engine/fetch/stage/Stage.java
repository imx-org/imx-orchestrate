package nl.geostandaarden.imx.orchestrate.engine.fetch.stage;

import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import nl.geostandaarden.imx.orchestrate.engine.exchange.BatchResult;
import nl.geostandaarden.imx.orchestrate.engine.exchange.CollectionResult;
import nl.geostandaarden.imx.orchestrate.engine.exchange.ObjectResult;
import nl.geostandaarden.imx.orchestrate.engine.selection.CompoundNode;
import reactor.core.publisher.Flux;

@Getter
@Builder(toBuilder = true)
public final class Stage {

    private final CompoundNode selection;

    private final boolean conditional;

    @Singular
    private final List<ObjectResult> nextResults;

    private final NextResultCombiner nextResultCombiner;

    @Singular
    @Getter(AccessLevel.NONE)
    private final List<NextStageCreator> nextStageCreators;

    @Singular
    @Getter(AccessLevel.NONE)
    private final List<ConditionalStageCreator> conditionalStageCreators;

    public Flux<Stage> getNextStages(ObjectResult result) {
        return Flux.fromIterable(nextStageCreators) //
                .flatMap(creator -> creator.create(result));
    }

    public Flux<Stage> getConditionalStages(ObjectResult result) {
        return Flux.fromIterable(conditionalStageCreators) //
                .flatMap(creator -> creator.create(result));
    }

    public Flux<Stage> getNextStages(CollectionResult result) {
        return Flux.fromIterable(nextStageCreators) //
                .flatMap(creator -> creator.create(result));
    }

    public Flux<Stage> getNextStages(BatchResult result) {
        return Flux.fromIterable(nextStageCreators) //
                .flatMap(creator -> creator.create(result));
    }

    public NextResultCombiner getNextResultCombiner() {
        if (nextResultCombiner == null) {
            return new NoopResultCombiner();
        }

        return nextResultCombiner;
    }
}
