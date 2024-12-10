package nl.geostandaarden.imx.orchestrate.source.file;

import static java.util.Collections.emptyList;
import static nl.geostandaarden.imx.orchestrate.source.file.FileUtils.getObjectProperties;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import nl.geostandaarden.imx.orchestrate.engine.exchange.BatchRequest;
import nl.geostandaarden.imx.orchestrate.engine.exchange.CollectionRequest;
import nl.geostandaarden.imx.orchestrate.engine.exchange.ObjectRequest;
import nl.geostandaarden.imx.orchestrate.engine.selection.CompoundNode;
import nl.geostandaarden.imx.orchestrate.engine.selection.ObjectNode;
import nl.geostandaarden.imx.orchestrate.engine.source.DataRepository;
import nl.geostandaarden.imx.orchestrate.model.ObjectType;
import nl.geostandaarden.imx.orchestrate.model.filters.FilterExpression;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

class FileRepository implements DataRepository {

    private final Map<String, Map<Map<String, Object>, com.fasterxml.jackson.databind.node.ObjectNode>> objectMap =
            new LinkedHashMap<>();

    @Override
    public Mono<Map<String, Object>> findOne(ObjectRequest request) {
        var selection = request.getSelection();

        var objectProperties = getObjectNodes(selection)
                .map(typeObjects -> typeObjects.get(selection.getObjectKey()))
                .map(objectNode -> getObjectProperties(objectNode, selection.getChildNodes()))
                .orElse(null);

        return Mono.justOrEmpty(objectProperties);
    }

    @Override
    public Flux<Map<String, Object>> find(CollectionRequest request) {
        var selection = request.getSelection();

        var objectList = getObjectNodes(selection).map(Map::values).orElse(emptyList()).stream()
                .filter(createFilter(selection.getFilter()))
                .map(objectNode -> getObjectProperties(objectNode, selection.getChildNodes()))
                .toList();

        return Flux.fromIterable(objectList);
    }

    @Override
    public Flux<Map<String, Object>> findBatch(BatchRequest request) {
        var selection = request.getSelection();

        return Flux.fromIterable(selection.getObjectKeys())
                .flatMap(objectKey -> findOne(ObjectRequest.builder()
                        .selection(ObjectNode.builder()
                                .childNodes(selection.getChildNodes())
                                .objectType(selection.getObjectType())
                                .objectKey(objectKey)
                                .build())
                        .build()));
    }

    @Override
    public boolean supportsBatchLoading(ObjectType objectType) {
        return true;
    }

    private Predicate<com.fasterxml.jackson.databind.node.ObjectNode> createFilter(FilterExpression filterExpression) {
        if (filterExpression == null) {
            return objectNode -> true;
        }

        var valueClassName = filterExpression.getValue().getClass().getName();

        // TODO: Handle special type-mapping
        if (valueClassName.startsWith("org.locationtech.jts.geom")) {
            return objectNode -> false;
        }

        var jsonValue = new ObjectMapper().valueToTree(filterExpression.getValue());

        var jsonPointer =
                JsonPointer.compile("/".concat(filterExpression.getPath().toString()));

        return objectNode -> {
            var propertyNode = objectNode.at(jsonPointer);

            if (propertyNode.isArray()) {
                var arrayElements = propertyNode.elements();

                while (arrayElements.hasNext()) {
                    if (jsonValue.equals(arrayElements.next())) {
                        return true;
                    }
                }

                return false;
            }

            return propertyNode.equals(jsonValue);
        };
    }

    public void add(
            String typeName, Map<String, Object> objectKey, com.fasterxml.jackson.databind.node.ObjectNode objectNode) {
        objectMap.putIfAbsent(typeName, new LinkedHashMap<>());
        objectMap.get(typeName).put(objectKey, objectNode);
    }

    private Optional<Map<Map<String, Object>, com.fasterxml.jackson.databind.node.ObjectNode>> getObjectNodes(
            CompoundNode selection) {
        return Optional.ofNullable(objectMap.get(selection.getObjectType().getName()));
    }
}
