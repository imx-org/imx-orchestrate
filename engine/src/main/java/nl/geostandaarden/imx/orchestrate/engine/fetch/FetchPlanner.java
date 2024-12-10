package nl.geostandaarden.imx.orchestrate.engine.fetch;

import java.util.Map;
import nl.geostandaarden.imx.orchestrate.engine.exchange.BatchRequest;
import nl.geostandaarden.imx.orchestrate.engine.exchange.CollectionRequest;
import nl.geostandaarden.imx.orchestrate.engine.exchange.ObjectRequest;
import nl.geostandaarden.imx.orchestrate.engine.exchange.ObjectResult;
import nl.geostandaarden.imx.orchestrate.engine.fetch.result.ObjectResultMapper;
import nl.geostandaarden.imx.orchestrate.engine.fetch.stage.StageExecutor;
import nl.geostandaarden.imx.orchestrate.engine.fetch.stage.StagePlanner;
import nl.geostandaarden.imx.orchestrate.engine.selection.TreeResolver;
import nl.geostandaarden.imx.orchestrate.engine.source.Source;
import nl.geostandaarden.imx.orchestrate.model.ModelMapping;
import reactor.core.publisher.Flux;

public final class FetchPlanner {

    private final ModelMapping modelMapping;

    private final TreeResolver treeResolver;

    private final StageExecutor stageExecutor;

    public FetchPlanner(ModelMapping modelMapping, Map<String, Source> sources, TreeResolver treeResolver) {
        this.modelMapping = modelMapping;
        this.treeResolver = treeResolver;
        this.stageExecutor = new StageExecutor(sources);
    }

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
