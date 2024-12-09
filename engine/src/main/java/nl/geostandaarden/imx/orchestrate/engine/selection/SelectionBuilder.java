package nl.geostandaarden.imx.orchestrate.engine.selection;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import nl.geostandaarden.imx.orchestrate.engine.OrchestrateException;
import nl.geostandaarden.imx.orchestrate.model.AbstractRelation;
import nl.geostandaarden.imx.orchestrate.model.Model;
import nl.geostandaarden.imx.orchestrate.model.ObjectType;
import nl.geostandaarden.imx.orchestrate.model.filters.FilterExpression;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SelectionBuilder {

    public static ObjectNodeBuilder newObjectNode(Model model, String typeName) {
        return new ObjectNodeBuilder(model, typeName);
    }

    public static ObjectNodeBuilder newObjectNode(Model model, AbstractRelation relation) {
        return new ObjectNodeBuilder(model, relation);
    }

    public static CollectionNodeBuilder newCollectionNode(Model model, String typeName) {
        return new CollectionNodeBuilder(model, typeName);
    }

    public static CollectionNodeBuilder newCollectionNode(Model model, AbstractRelation relation) {
        return new CollectionNodeBuilder(model, relation);
    }

    public static BatchNodeBuilder newBatchNode(Model model, String typeName) {
        return new BatchNodeBuilder(model, typeName);
    }

    public static BatchNodeBuilder newBatchNode(Model model, AbstractRelation relation) {
        return new BatchNodeBuilder(model, relation);
    }

    private abstract static class AbstractNodeBuilder<T extends AbstractNodeBuilder<T>> {

        protected final Model model;

        protected final ObjectType objectType;

        protected final Map<String, TreeNode> childNodes = new LinkedHashMap<>();

        private AbstractNodeBuilder(Model model, String typeName) {
            this.model = model;
            this.objectType = model.getObjectType(typeName);
        }

        protected abstract T self();

        public T select(String name) {
            var attribute = objectType.getAttribute(name);
            childNodes.put(name, AttributeNode.forAttribute(attribute));
            return self();
        }

        public T selectObject(String name, Function<ObjectNodeBuilder, ObjectNode> builderFn) {
            var relation = objectType.getRelation(name);

            if (!relation.getMultiplicity().isSingular()) {
                throw new OrchestrateException("Relation is not singular: " + name);
            }

            childNodes.put(name, builderFn.apply(newObjectNode(model, relation)));
            return self();
        }

        public T selectCollection(String name, Function<CollectionNodeBuilder, CollectionNode> builderFn) {
            var relation = objectType.getRelation(name);

            if (relation.getMultiplicity().isSingular()) {
                throw new OrchestrateException("Relation is singular: " + name);
            }

            childNodes.put(name, builderFn.apply(newCollectionNode(model, relation)));
            return self();
        }
    }

    public static class ObjectNodeBuilder extends AbstractNodeBuilder<ObjectNodeBuilder> {

        private final ObjectNode.ObjectNodeBuilder<?, ?> builder;

        private ObjectNodeBuilder(Model model, String typeName) {
            super(model, typeName);

            builder = ObjectNode.builder() //
                    .objectType(objectType);
        }

        private ObjectNodeBuilder(Model model, AbstractRelation relation) {
            super(model, relation.getTarget().getName());

            builder = ObjectNode.builder() //
                    .relation(relation)
                    .objectType(objectType);
        }

        protected ObjectNodeBuilder self() {
            return this;
        }

        public ObjectNodeBuilder objectKey(Map<String, Object> objectKey) {
            builder.objectKey(objectKey);
            return this;
        }

        public ObjectNode build() {
            return builder.childNodes(childNodes).build();
        }
    }

    public static class CollectionNodeBuilder extends AbstractNodeBuilder<CollectionNodeBuilder> {

        private final CollectionNode.CollectionNodeBuilder<?, ?> builder;

        private CollectionNodeBuilder(Model model, String typeName) {
            super(model, typeName);

            builder = CollectionNode.builder().objectType(objectType);
        }

        private CollectionNodeBuilder(Model model, AbstractRelation relation) {
            super(model, relation.getTarget().getName());

            builder = CollectionNode.builder() //
                    .relation(relation)
                    .objectType(objectType);
        }

        protected CollectionNodeBuilder self() {
            return this;
        }

        public CollectionNodeBuilder filter(FilterExpression filter) {
            builder.filter(filter);
            return this;
        }

        public CollectionNode build() {
            return builder.childNodes(childNodes).build();
        }
    }

    public static class BatchNodeBuilder extends AbstractNodeBuilder<BatchNodeBuilder> {

        private final BatchNode.BatchNodeBuilder<?, ?> builder;

        private BatchNodeBuilder(Model model, String typeName) {
            super(model, typeName);

            builder = BatchNode.builder().objectType(objectType);
        }

        private BatchNodeBuilder(Model model, AbstractRelation relation) {
            super(model, relation.getTarget().getName());

            builder = BatchNode.builder() //
                    .relation(relation)
                    .objectType(objectType);
        }

        protected BatchNodeBuilder self() {
            return this;
        }

        public BatchNodeBuilder objectKey(Map<String, Object> objectKey) {
            builder.objectKey(objectKey);
            return this;
        }

        public BatchNode build() {
            return builder.childNodes(childNodes).build();
        }
    }
}
