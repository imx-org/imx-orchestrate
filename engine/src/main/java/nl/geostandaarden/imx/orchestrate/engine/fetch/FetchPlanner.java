package nl.geostandaarden.imx.orchestrate.engine.fetch;

import lombok.RequiredArgsConstructor;
import nl.geostandaarden.imx.orchestrate.engine.exchange.BatchRequest;
import nl.geostandaarden.imx.orchestrate.engine.exchange.CollectionRequest;
import nl.geostandaarden.imx.orchestrate.engine.exchange.ObjectRequest;
import nl.geostandaarden.imx.orchestrate.engine.exchange.ObjectResult;
import nl.geostandaarden.imx.orchestrate.engine.fetch.result.ObjectResultMapper;
import nl.geostandaarden.imx.orchestrate.engine.fetch.stage.StageExecutor;
import nl.geostandaarden.imx.orchestrate.engine.fetch.stage.StagePlanner;
import nl.geostandaarden.imx.orchestrate.engine.selection.TreeResolver;
import nl.geostandaarden.imx.orchestrate.model.ModelMapping;
import reactor.core.publisher.Flux;

@RequiredArgsConstructor
public final class FetchPlanner {

    private final ModelMapping modelMapping;

    private final TreeResolver treeResolver;

    private final StageExecutor stageExecutor = new StageExecutor();

    public Flux<ObjectResult> fetch(ObjectRequest request) {
        var typeMappings =
                modelMapping.getObjectTypeMappings(request.getSelection().getObjectType());

        return Flux.fromIterable(typeMappings).flatMapSequential(typeMapping -> {
            var selection = treeResolver.resolve(request.getSelection(), typeMapping);
            var stage = new StagePlanner().plan(selection);

            var resultMapper = ObjectResultMapper.builder() //
                    .modelMapping(modelMapping)
                    .build();

            return stageExecutor //
                    .execute(stage)
                    .map(result -> resultMapper.map(result, request.getSelection()));
        });
    }

    public Flux<ObjectResult> fetch(CollectionRequest request) {
        var typeMappings =
                modelMapping.getObjectTypeMappings(request.getSelection().getObjectType());

        return Flux.fromIterable(typeMappings).flatMapSequential(typeMapping -> {
            var selection = treeResolver.resolve(request.getSelection(), typeMapping);
            var stage = new StagePlanner().plan(selection);

            var resultMapper = ObjectResultMapper.builder() //
                    .modelMapping(modelMapping)
                    .build();

            return stageExecutor //
                    .execute(stage)
                    .map(result -> resultMapper.map(result, request.getSelection()));
        });
    }

    public Flux<ObjectResult> fetch(BatchRequest request) {
        var typeMappings =
                modelMapping.getObjectTypeMappings(request.getSelection().getObjectType());

        return Flux.fromIterable(typeMappings).flatMapSequential(typeMapping -> {
            var selection = treeResolver.resolve(request.getSelection(), typeMapping);
            var stage = new StagePlanner().plan(selection);

            var resultMapper = ObjectResultMapper.builder() //
                    .modelMapping(modelMapping)
                    .build();

            return stageExecutor //
                    .execute(stage)
                    .map(result -> resultMapper.map(result, request.getSelection()));
        });
    }
}
