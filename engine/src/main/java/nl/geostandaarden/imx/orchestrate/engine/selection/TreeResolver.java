package nl.geostandaarden.imx.orchestrate.engine.selection;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import nl.geostandaarden.imx.orchestrate.engine.exchange.CollectionRequest;
import nl.geostandaarden.imx.orchestrate.engine.exchange.ObjectRequest;
import nl.geostandaarden.imx.orchestrate.engine.exchange.SelectedProperty;
import nl.geostandaarden.imx.orchestrate.engine.source.Source;
import nl.geostandaarden.imx.orchestrate.model.Attribute;
import nl.geostandaarden.imx.orchestrate.model.ModelMapping;
import nl.geostandaarden.imx.orchestrate.model.ObjectTypeMapping;
import nl.geostandaarden.imx.orchestrate.model.ObjectTypeRef;
import nl.geostandaarden.imx.orchestrate.model.Path;
import nl.geostandaarden.imx.orchestrate.model.PathMapping;
import nl.geostandaarden.imx.orchestrate.model.Relation;

@RequiredArgsConstructor
public final class TreeResolver {

    private final ModelMapping modelMapping;

    private final Map<String, Source> sources;

    public ObjectNode resolve(ObjectRequest request, ObjectTypeMapping typeMapping) {
        var sourcePaths = request.getSelectedProperties().stream()
                .flatMap(selectedProperty -> resolveSourcePaths(selectedProperty, typeMapping))
                .collect(Collectors.toSet());

        return createObjectNode(typeMapping.getSourceRoot(), sourcePaths, request.getObjectKey());
    }

    public CollectionNode resolve(CollectionRequest request, ObjectTypeMapping typeMapping) {
        var sourcePaths = request.getSelectedProperties().stream()
                .flatMap(selectedProperty -> resolveSourcePaths(selectedProperty, typeMapping))
                .collect(Collectors.toSet());

        return createCollectionNode(typeMapping.getSourceRoot(), sourcePaths);
    }

    private Stream<Path> resolveSourcePaths(SelectedProperty selectedProperty, ObjectTypeMapping typeMapping) {
        return typeMapping.getPropertyMapping(selectedProperty.getProperty()).stream()
                .flatMap(propertyMapping ->
                        propertyMapping.getPathMappings().stream().map(PathMapping::getPath));
    }

    private ObjectNode createObjectNode(
            ObjectTypeRef sourceTypeRef, Set<Path> sourcePaths, Map<String, Object> objectKey) {
        var childNodes = createChildNodes(sourceTypeRef, sourcePaths);
        var sourceType = modelMapping.getSourceType(sourceTypeRef);
        var source = sources.get(sourceTypeRef.getModelAlias());

        return ObjectNode.builder()
                .childNodes(childNodes)
                .objectType(sourceType)
                .objectKey(objectKey)
                .source(source)
                .build();
    }

    private CollectionNode createCollectionNode(ObjectTypeRef sourceTypeRef, Set<Path> sourcePaths) {
        var childNodes = createChildNodes(sourceTypeRef, sourcePaths);
        var sourceType = modelMapping.getSourceType(sourceTypeRef);
        var source = sources.get(sourceTypeRef.getModelAlias());

        return CollectionNode.builder()
                .childNodes(childNodes)
                .objectType(sourceType)
                .source(source)
                .build();
    }

    private Map<String, TreeNode> createChildNodes(ObjectTypeRef sourceTypeRef, Set<Path> sourcePaths) {
        var sourceType = modelMapping.getSourceType(sourceTypeRef);
        var childNodes = new LinkedHashMap<String, TreeNode>();

        sourcePaths.stream()
                .collect(Collectors.groupingBy(
                        Path::getFirstSegment, //
                        Collectors.mapping(Path::withoutFirstSegment, Collectors.toSet())))
                .forEach((name, nestedPaths) -> {
                    var property = sourceType.getProperty(name);

                    if (property instanceof Attribute attribute) {
                        childNodes.put(name, AttributeNode.forAttribute(attribute));
                    } else if (property instanceof Relation relation) {
                        var childType = relation.getTarget(sourceTypeRef);

                        if (relation.getMultiplicity().isSingular()) {
                            childNodes.put(name, createObjectNode(childType, nestedPaths, null));
                        } else {
                            childNodes.put(name, createCollectionNode(childType, nestedPaths));
                        }
                    }
                });

        return Collections.unmodifiableMap(childNodes);
    }
}
