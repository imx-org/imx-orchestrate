package nl.geostandaarden.imx.orchestrate.gateway.fetch;

import static graphql.schema.GraphQLTypeUtil.isScalar;
import static graphql.schema.GraphQLTypeUtil.unwrapAll;
import static graphql.schema.GraphQLTypeUtil.unwrapNonNull;
import static java.util.function.Predicate.not;
import static nl.geostandaarden.imx.orchestrate.engine.fetch.FetchUtils.cast;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.DataFetchingFieldSelectionSet;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLObjectType;
import graphql.schema.SelectedField;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import nl.geostandaarden.imx.orchestrate.engine.OrchestrateEngine;
import nl.geostandaarden.imx.orchestrate.engine.OrchestrateException;
import nl.geostandaarden.imx.orchestrate.engine.exchange.BatchRequest;
import nl.geostandaarden.imx.orchestrate.engine.exchange.CollectionRequest;
import nl.geostandaarden.imx.orchestrate.engine.exchange.DataRequest;
import nl.geostandaarden.imx.orchestrate.engine.exchange.DataResult;
import nl.geostandaarden.imx.orchestrate.engine.exchange.ObjectRequest;
import nl.geostandaarden.imx.orchestrate.engine.selection.AttributeNode;
import nl.geostandaarden.imx.orchestrate.engine.selection.BatchNode;
import nl.geostandaarden.imx.orchestrate.engine.selection.CollectionNode;
import nl.geostandaarden.imx.orchestrate.engine.selection.ObjectNode;
import nl.geostandaarden.imx.orchestrate.engine.selection.TreeNode;
import nl.geostandaarden.imx.orchestrate.gateway.schema.SchemaConstants;
import nl.geostandaarden.imx.orchestrate.model.Attribute;
import nl.geostandaarden.imx.orchestrate.model.Model;
import nl.geostandaarden.imx.orchestrate.model.ObjectType;
import nl.geostandaarden.imx.orchestrate.model.Path;
import nl.geostandaarden.imx.orchestrate.model.filters.FilterExpression;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public final class GenericDataFetcher implements DataFetcher<Mono<? extends DataResult>> {

    private final OrchestrateEngine engine;

    private final String hasLineageFieldName;

    @Override
    public Mono<? extends DataResult> get(DataFetchingEnvironment environment) {
        var request = createRequest(environment);

        if (request instanceof ObjectRequest objectRequest) {
            return engine.fetch(objectRequest);
        }

        if (request instanceof CollectionRequest collectionRequest) {
            return engine.fetch(collectionRequest);
        }

        if (request instanceof BatchRequest batchRequest) {
            return engine.fetch(batchRequest);
        }

        throw new OrchestrateException("Unsupported request: " + request.getClass());
    }

    private boolean isReservedField(SelectedField selectedField) {
        var name = selectedField.getName();
        return hasLineageFieldName.equals(name) || name.startsWith("__");
    }

    private DataRequest<?> createRequest(DataFetchingEnvironment environment) {
        var fieldName = environment.getField().getName();
        var targetModel = engine.getModelMapping().getTargetModel();
        var targetType =
                targetModel.getObjectType(unwrapAll(environment.getFieldType()).getName());
        var childNodes = getChildNodes(targetModel, targetType, environment.getSelectionSet());

        if (fieldName.endsWith(SchemaConstants.QUERY_COLLECTION_SUFFIX)) {
            Map<String, Object> filterArg = environment.getArgument(SchemaConstants.QUERY_FILTER_ARGUMENT);

            var filter = filterArg != null ? createFilterExpression(targetType, filterArg) : null;

            var selection = CollectionNode.builder()
                    .childNodes(childNodes)
                    .objectType(targetType)
                    .filter(filter)
                    .build();

            return CollectionRequest.builder() //
                    .selection(selection)
                    .build();
        }

        if (fieldName.endsWith(SchemaConstants.QUERY_BATCH_SUFFIX)) {
            List<Map<String, Object>> objectKeys =
                    cast(environment.getArguments().get(SchemaConstants.BATCH_KEYS_ARG));

            var selection = BatchNode.builder()
                    .childNodes(childNodes)
                    .objectType(targetType)
                    .objectKeys(objectKeys)
                    .build();

            return BatchRequest.builder() //
                    .selection(selection)
                    .build();
        }

        var selection = ObjectNode.builder()
                .childNodes(childNodes)
                .objectType(targetType)
                .objectKey(environment.getArguments())
                .build();

        return ObjectRequest.builder() //
                .selection(selection)
                .build();
    }

    private Map<String, TreeNode> getChildNodes(
            Model model, ObjectType targetType, DataFetchingFieldSelectionSet selectionSet) {
        var childNodes = new LinkedHashMap<String, TreeNode>();

        selectionSet.getImmediateFields().stream()
                .filter(not(this::isReservedField))
                .forEach(selectedField -> {
                    var fieldName = selectedField.getName();
                    var fieldType = unwrapNonNull(selectedField.getType());

                    if (fieldType instanceof GraphQLObjectType) {
                        var relation = targetType.getRelation(fieldName);
                        var nestedType = model.getObjectType(relation.getTarget());

                        childNodes.put(
                                fieldName,
                                ObjectNode.builder()
                                        .objectType(nestedType)
                                        .childNodes(getChildNodes(model, nestedType, selectedField.getSelectionSet()))
                                        .build());
                    } else if (fieldType instanceof GraphQLList && !isScalar(unwrapAll(fieldType))) {
                        var relation = targetType.getRelation(fieldName);
                        var nestedType = model.getObjectType(relation.getTarget());

                        childNodes.put(
                                fieldName,
                                CollectionNode.builder()
                                        .objectType(nestedType)
                                        .childNodes(getChildNodes(model, nestedType, selectedField.getSelectionSet()))
                                        .build());
                    } else {
                        var attribute = targetType.getAttribute(fieldName);
                        childNodes.put(fieldName, AttributeNode.forAttribute(attribute));
                    }
                });

        return Collections.unmodifiableMap(childNodes);
    }

    private FilterExpression createFilterExpression(ObjectType targetType, Map<String, Object> arguments) {
        if (arguments.size() > 1) {
            throw new OrchestrateException("Currently only a single filter property is supported.");
        }

        var firstEntry = arguments.entrySet().iterator().next();

        var propertyName = firstEntry.getKey();
        var property = targetType.getProperty(propertyName);

        if (property instanceof Attribute attribute) {
            return attribute
                    .getType()
                    .createFilterExpression(Path.fromProperties(property), cast(arguments.get(propertyName)));
        }

        throw new OrchestrateException("Currently only attributes can be filtered.");
    }
}
