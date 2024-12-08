package nl.geostandaarden.imx.orchestrate.engine.stage;

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

    private final NextResultCombiner nextResultCombiner;

    @Singular
    @Getter(AccessLevel.NONE)
    private final List<NextStageCreator> nextStageCreators;

    public Flux<Stage> getNextStages(ObjectResult result) {
        return Flux.fromIterable(nextStageCreators) //
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
}
