package org.dotwebstack.orchestrate.source.file;

import static java.util.Collections.emptyList;
import static org.dotwebstack.orchestrate.source.file.FileUtils.getObjectProperties;

import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import org.dotwebstack.orchestrate.source.CollectionRequest;
import org.dotwebstack.orchestrate.source.DataRepository;
import org.dotwebstack.orchestrate.source.DataRequest;
import org.dotwebstack.orchestrate.source.ObjectRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

class FileRepository implements DataRepository {

  private final Map<String, Map<Map<String, Object>, ObjectNode>> objectMap = new LinkedHashMap<>();

  @Override
  public Mono<Map<String, Object>> findOne(ObjectRequest objectRequest) {
    var objectProperties = getObjectNodes(objectRequest)
        .map(typeObjects -> typeObjects.get(objectRequest.getObjectKey()))
        .map(objectNode -> getObjectProperties(objectNode, objectRequest.getSelectedProperties()))
        .orElse(null);

    return Mono.justOrEmpty(objectProperties);
  }

  @Override
  public Flux<Map<String, Object>> find(CollectionRequest collectionRequest) {
    var objectList = getObjectNodes(collectionRequest)
        .map(Map::values)
        .orElse(emptyList())
        .stream()
        .map(objectNode -> getObjectProperties(objectNode, collectionRequest.getSelectedProperties()))
        .toList();

    return Flux.fromIterable(objectList);
  }

  public void add(String typeName, Map<String, Object> objectKey, ObjectNode objectNode) {
    objectMap.putIfAbsent(typeName, new LinkedHashMap<>());
    objectMap.get(typeName)
        .put(objectKey, objectNode);
  }

  private Optional<Map<Map<String, Object>, ObjectNode>> getObjectNodes(DataRequest dataRequest) {
    return Optional.ofNullable(objectMap.get(dataRequest.getObjectType()
        .getName()));
  }
}
