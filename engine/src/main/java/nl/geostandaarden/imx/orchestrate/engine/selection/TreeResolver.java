package nl.geostandaarden.imx.orchestrate.engine.selection;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import nl.geostandaarden.imx.orchestrate.model.AbstractRelation;
import nl.geostandaarden.imx.orchestrate.model.Attribute;
import nl.geostandaarden.imx.orchestrate.model.ConditionalMapping;
import nl.geostandaarden.imx.orchestrate.model.ModelMapping;
import nl.geostandaarden.imx.orchestrate.model.ObjectTypeMapping;
import nl.geostandaarden.imx.orchestrate.model.ObjectTypeRef;
import nl.geostandaarden.imx.orchestrate.model.Path;
import nl.geostandaarden.imx.orchestrate.model.PathMapping;

@RequiredArgsConstructor
public final class TreeResolver {

    private final ModelMapping modelMapping;

    public ObjectNode resolve(ObjectNode selection, ObjectTypeMapping typeMapping) {
        var sourcePaths = resolveSourcePaths(selection, typeMapping);

        return createObjectNode(typeMapping.getSourceRoot(), sourcePaths, null, selection.getObjectKey());
    }

    public CollectionNode resolve(CollectionNode selection, ObjectTypeMapping typeMapping) {
        var sourcePaths = resolveSourcePaths(selection, typeMapping);

        return createCollectionNode(typeMapping.getSourceRoot(), sourcePaths, null);
    }

    public BatchNode resolve(BatchNode selection, ObjectTypeMapping typeMapping) {
        var sourcePaths = resolveSourcePaths(selection, typeMapping);

        return createBatchNode(typeMapping.getSourceRoot(), sourcePaths, null, selection.getObjectKeys());
    }

    public Set<Path> resolveSourcePaths(ConditionalMapping conditionalMapping) {
        return conditionalMapping.getWhen().stream()
                .flatMap(when -> when.getPathMappings().stream())
                .map(PathMapping::getPath)
                .collect(Collectors.toUnmodifiableSet());
    }

    private Set<Path> resolveSourcePaths(CompoundNode selection, ObjectTypeMapping typeMapping) {
        return selection.getChildNodes().entrySet().stream()
                .flatMap(entry -> resolveSourcePaths(entry.getKey(), entry.getValue(), typeMapping))
                .collect(Collectors.toSet());
    }

    private Stream<Path> resolveSourcePaths(String propertyName, TreeNode propertyNode, ObjectTypeMapping typeMapping) {
        var propertyMapping = typeMapping //
                .getPropertyMapping(propertyName)
                .orElse(null);

        if (propertyMapping == null) {
            if (propertyNode instanceof CompoundNode compoundNode) {
                return modelMapping.getObjectTypeMappings(compoundNode.getObjectType()).stream()
                        .filter(nestedTypeMapping ->
                                nestedTypeMapping.getSourceRoot().equals(typeMapping.getSourceRoot()))
                        .flatMap(nestedTypeMapping -> resolveSourcePaths(compoundNode, nestedTypeMapping).stream());
            }

            return Stream.empty();
        }

        var paths = propertyMapping //
                .getPathMappings()
                .stream()
                .map(PathMapping::getPath);

        if (propertyNode instanceof CompoundNode compoundNode) {
            var pathList = paths.toList();

            return modelMapping.getObjectTypeMappings(compoundNode.getObjectType()).stream()
                    .flatMap(childTypeMapping -> compoundNode.getChildNodes().entrySet().stream()
                            .flatMap(entry -> resolveSourcePaths(entry.getKey(), entry.getValue(), childTypeMapping)))
                    .flatMap(childPath -> pathList.stream() //
                            .map(path -> path.append(childPath)));
        }

        return paths;
    }

    public ObjectNode createObjectNode(
            ObjectTypeRef sourceTypeRef,
            Set<Path> sourcePaths,
            AbstractRelation relation,
            Map<String, Object> objectKey) {
        var childNodes = createChildNodes(sourceTypeRef, sourcePaths);
        var sourceType = modelMapping.getSourceType(sourceTypeRef);

        return ObjectNode.builder()
                .relation(relation)
                .childNodes(childNodes)
                .objectType(sourceType)
                .objectKey(objectKey)
                .modelAlias(sourceTypeRef.getModelAlias())
                .build();
    }

    private CollectionNode createCollectionNode(
            ObjectTypeRef sourceTypeRef, Set<Path> sourcePaths, AbstractRelation relation) {
        var childNodes = createChildNodes(sourceTypeRef, sourcePaths);
        var sourceType = modelMapping.getSourceType(sourceTypeRef);

        return CollectionNode.builder()
                .relation(relation)
                .childNodes(childNodes)
                .objectType(sourceType)
                .modelAlias(sourceTypeRef.getModelAlias())
                .build();
    }

    private BatchNode createBatchNode(
            ObjectTypeRef sourceTypeRef,
            Set<Path> sourcePaths,
            AbstractRelation relation,
            Collection<Map<String, Object>> objectKeys) {
        var childNodes = createChildNodes(sourceTypeRef, sourcePaths);
        var sourceType = modelMapping.getSourceType(sourceTypeRef);

        return BatchNode.builder()
                .relation(relation)
                .childNodes(childNodes)
                .objectType(sourceType)
                .objectKeys(objectKeys)
                .modelAlias(sourceTypeRef.getModelAlias())
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
                    } else if (property instanceof AbstractRelation relation) {
                        var childType = relation.getTarget(sourceTypeRef);

                        if (relation.getMultiplicity().isSingular()) {
                            childNodes.put(name, createObjectNode(childType, nestedPaths, relation, null));
                        } else {
                            childNodes.put(name, createCollectionNode(childType, nestedPaths, relation));
                        }
                    }
                });

        return Collections.unmodifiableMap(childNodes);
    }
}
