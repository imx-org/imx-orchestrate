package org.dotwebstack.orchestrate.engine.schema;

import static graphql.language.FieldDefinition.newFieldDefinition;
import static graphql.language.ObjectTypeDefinition.newObjectTypeDefinition;
import static graphql.schema.FieldCoordinates.coordinates;
import static graphql.schema.GraphQLCodeRegistry.newCodeRegistry;
import static graphql.schema.SchemaTransformer.transformSchema;
import static graphql.schema.idl.RuntimeWiring.newRuntimeWiring;
import static org.apache.commons.lang3.StringUtils.uncapitalize;
import static org.dotwebstack.orchestrate.engine.schema.SchemaConstants.QUERY_COLLECTION_SUFFIX;
import static org.dotwebstack.orchestrate.engine.schema.SchemaConstants.QUERY_TYPE;
import static org.dotwebstack.orchestrate.engine.schema.SchemaUtils.requiredListType;
import static org.dotwebstack.orchestrate.engine.schema.SchemaUtils.requiredType;
import static org.dotwebstack.orchestrate.model.Cardinality.REQUIRED;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.module.SimpleModule;
import graphql.language.FieldDefinition;
import graphql.language.InputValueDefinition;
import graphql.language.NonNullType;
import graphql.language.ObjectTypeDefinition;
import graphql.language.Type;
import graphql.language.TypeName;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.TypeDefinitionRegistry;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.UnaryOperator;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.dotwebstack.orchestrate.engine.OrchestrateExtension;
import org.dotwebstack.orchestrate.engine.Orchestration;
import org.dotwebstack.orchestrate.engine.fetch.FetchPlanner;
import org.dotwebstack.orchestrate.engine.fetch.GenericDataFetcher;
import org.dotwebstack.orchestrate.engine.fetch.ObjectKeyFetcher;
import org.dotwebstack.orchestrate.engine.fetch.PropertyValueFetcher;
import org.dotwebstack.orchestrate.model.Attribute;
import org.dotwebstack.orchestrate.model.ModelMapping;
import org.dotwebstack.orchestrate.model.ObjectType;
import org.dotwebstack.orchestrate.model.Relation;
import org.dotwebstack.orchestrate.model.lineage.ObjectLineage;
import org.dotwebstack.orchestrate.model.lineage.ObjectReference;
import org.dotwebstack.orchestrate.model.lineage.OrchestratedProperty;
import org.dotwebstack.orchestrate.model.lineage.PropertyMapping;
import org.dotwebstack.orchestrate.model.lineage.PropertyMappingExecution;
import org.dotwebstack.orchestrate.model.lineage.PropertyPath;
import org.dotwebstack.orchestrate.model.lineage.PropertyPathMapping;
import org.dotwebstack.orchestrate.model.lineage.SourceProperty;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class SchemaFactory {

  private final TypeDefinitionRegistry typeDefinitionRegistry = new TypeDefinitionRegistry();

  private final GraphQLCodeRegistry.Builder codeRegistryBuilder = newCodeRegistry();

  private final ObjectTypeDefinition.Builder queryTypeBuilder = newObjectTypeDefinition().name(QUERY_TYPE);

  private final ModelMapping modelMapping;

  private final GenericDataFetcher genericDataFetcher;

  private final UnaryOperator<String> lineageRenamer;

  private final Set<OrchestrateExtension> extensions;

  public static GraphQLSchema create(Orchestration orchestration) {
    var modelMapping = orchestration.getModelMapping();
    var lineageMapper = getObjectMapperInstance(modelMapping, orchestration.getExtensions());

    UnaryOperator<String> lineageRenamer =
        fieldName -> modelMapping.getLineageNameMapping().getOrDefault(fieldName, fieldName);

    var fetchPlanner = new FetchPlanner(modelMapping, orchestration.getSources(), lineageMapper, lineageRenamer);
    var genericDataFetcher = new GenericDataFetcher(fetchPlanner);

    return new SchemaFactory(modelMapping, genericDataFetcher, lineageRenamer, orchestration.getExtensions()).create();
  }

  private static ObjectMapper getObjectMapperInstance(ModelMapping modelMapping, Set<OrchestrateExtension> extensions) {
    var lineageMapping = modelMapping.getLineageNameMapping();
    var objectMapper = new ObjectMapper();

    if (!lineageMapping.isEmpty()) {
      var module = new SimpleModule()
          .setDeserializerModifier(new BeanDeserializerModifier() {
            @Override
            public JsonDeserializer<?> modifyDeserializer(DeserializationConfig config, BeanDescription beanDesc,
                JsonDeserializer<?> deserializer) {
              return new FieldRenamingDeserializer(deserializer, lineageMapping);
            }
          });

      objectMapper.registerModule(module);
    }

    extensions.stream()
        .map(OrchestrateExtension::getLineageSerializerModule)
        .filter(Objects::nonNull)
        .forEach(objectMapper::registerModule);

    return objectMapper;
  }

  private GraphQLSchema create() {
    registerLineageTypes();

    modelMapping.getTargetModel()
        .getObjectTypes()
        .forEach(this::registerObjectType);

    typeDefinitionRegistry.add(queryTypeBuilder.build());
    extensions.forEach(extension -> extension.enhanceSchema(typeDefinitionRegistry, codeRegistryBuilder));

    var runtimeWiring = newRuntimeWiring()
        .codeRegistry(codeRegistryBuilder.build())
        .build();

    var schema = new SchemaGenerator()
        .makeExecutableSchema(typeDefinitionRegistry, runtimeWiring);

    return transformSchema(schema, new SchemaVisitor());
  }

  private void registerObjectType(ObjectType objectType) {
    var objectTypeDefinition = createObjectTypeDefinition(objectType);
    typeDefinitionRegistry.add(objectTypeDefinition);

    var baseName = uncapitalize(objectTypeDefinition.getName());
    var collectionName = baseName.concat(QUERY_COLLECTION_SUFFIX);

    queryTypeBuilder.fieldDefinition(newFieldDefinition()
            .name(baseName)
            .type(new TypeName(objectTypeDefinition.getName()))
            .inputValueDefinitions(createIdentityArguments(objectType))
            .build())
        .fieldDefinition(newFieldDefinition()
            .name(collectionName)
            .type(requiredListType(objectTypeDefinition.getName()))
            .build());

    codeRegistryBuilder.dataFetcher(coordinates(QUERY_TYPE, baseName), genericDataFetcher)
        .dataFetcher(coordinates(QUERY_TYPE, collectionName), genericDataFetcher);
  }

  private void registerLineageTypes() {
    typeDefinitionRegistry.add(newObjectTypeDefinition()
        .name(lineageRenamer.apply(ObjectLineage.class.getSimpleName()))
        .fieldDefinition(newFieldDefinition()
            .name(lineageRenamer.apply("orchestratedProperties"))
            .type(requiredListType(lineageRenamer.apply(OrchestratedProperty.class.getSimpleName())))
            .build())
        .build());

    typeDefinitionRegistry.add(newObjectTypeDefinition()
        .name(lineageRenamer.apply(OrchestratedProperty.class.getSimpleName()))
        .fieldDefinition(newFieldDefinition()
            .name(lineageRenamer.apply("subject"))
            .type(requiredType(lineageRenamer.apply(ObjectReference.class.getSimpleName())))
            .build())
        .fieldDefinition(newFieldDefinition()
            .name(lineageRenamer.apply("property"))
            .type(requiredType("String"))
            .build())
        .fieldDefinition(newFieldDefinition()
            .name(lineageRenamer.apply("value"))
            .type(requiredType("PropertyValue"))
            .build())
        .fieldDefinition(newFieldDefinition()
            .name(lineageRenamer.apply("wasGeneratedBy"))
            .type(requiredType(lineageRenamer.apply(PropertyMappingExecution.class.getSimpleName())))
            .build())
        .fieldDefinition(newFieldDefinition()
            .name(lineageRenamer.apply("isDerivedFrom"))
            .type(requiredListType(lineageRenamer.apply(SourceProperty.class.getSimpleName())))
            .build())
        .build());

    typeDefinitionRegistry.add(newObjectTypeDefinition()
        .name(lineageRenamer.apply(PropertyMappingExecution.class.getSimpleName()))
        .fieldDefinition(newFieldDefinition()
            .name(lineageRenamer.apply("used"))
            .type(requiredType(lineageRenamer.apply(PropertyMapping.class.getSimpleName())))
            .build())
        .build());

    typeDefinitionRegistry.add(newObjectTypeDefinition()
        .name(lineageRenamer.apply(PropertyMapping.class.getSimpleName()))
        .fieldDefinition(newFieldDefinition()
            .name(lineageRenamer.apply("pathMapping"))
            .type(requiredListType(lineageRenamer.apply(PropertyPathMapping.class.getSimpleName())))
            .build())
        .build());

    typeDefinitionRegistry.add(newObjectTypeDefinition()
        .name(lineageRenamer.apply(PropertyPathMapping.class.getSimpleName()))
        .fieldDefinition(newFieldDefinition()
            .name(lineageRenamer.apply("path"))
            .type(requiredListType(lineageRenamer.apply(PropertyPath.class.getSimpleName())))
            .build())
        .build());

    typeDefinitionRegistry.add(newObjectTypeDefinition()
        .name(lineageRenamer.apply(PropertyPath.class.getSimpleName()))
        .fieldDefinition(newFieldDefinition()
            .name(lineageRenamer.apply("startNode"))
            .type(requiredType(lineageRenamer.apply(ObjectReference.class.getSimpleName())))
            .build())
        .fieldDefinition(newFieldDefinition()
            .name(lineageRenamer.apply("segments"))
            .type(requiredListType("String"))
            .build())
        .fieldDefinition(newFieldDefinition()
            .name(lineageRenamer.apply("references"))
            .type(requiredListType(lineageRenamer.apply(SourceProperty.class.getSimpleName())))
            .build())
        .build());

    typeDefinitionRegistry.add(newObjectTypeDefinition()
        .name(lineageRenamer.apply(SourceProperty.class.getSimpleName()))
        .fieldDefinition(newFieldDefinition()
            .name(lineageRenamer.apply("subject"))
            .type(requiredType(lineageRenamer.apply(ObjectReference.class.getSimpleName())))
            .build())
        .fieldDefinition(newFieldDefinition()
            .name(lineageRenamer.apply("property"))
            .type(requiredType("String"))
            .build())
        .fieldDefinition(newFieldDefinition()
            .name(lineageRenamer.apply("propertyPath"))
            .type(requiredListType("String"))
            .build())
        .fieldDefinition(newFieldDefinition()
            .name(lineageRenamer.apply("value"))
            .type(requiredType(lineageRenamer.apply("PropertyValue")))
            .build())
        .build());

    typeDefinitionRegistry.add(newObjectTypeDefinition()
        .name(lineageRenamer.apply(ObjectReference.class.getSimpleName()))
        .fieldDefinition(newFieldDefinition()
            .name(lineageRenamer.apply("objectType"))
            .type(requiredType("String"))
            .build())
        .fieldDefinition(newFieldDefinition()
            .name(lineageRenamer.apply("objectKey"))
            .type(requiredType("String"))
            .build())
        .build());

    typeDefinitionRegistry.add(newObjectTypeDefinition()
        .name(lineageRenamer.apply("PropertyValue"))
        .fieldDefinition(newFieldDefinition()
            .name(lineageRenamer.apply("stringValue"))
            .type(new TypeName("String"))
            .build())
        .fieldDefinition(newFieldDefinition()
            .name(lineageRenamer.apply("integerValue"))
            .type(new TypeName("Int"))
            .build())
        .fieldDefinition(newFieldDefinition()
            .name(lineageRenamer.apply("booleanValue"))
            .type(new TypeName("Boolean"))
            .build())
        .build());

    var valueFetcher = new PropertyValueFetcher(lineageRenamer);

    var objectKeyFetcher = new ObjectKeyFetcher(lineageRenamer);

    codeRegistryBuilder.dataFetcher(coordinates(lineageRenamer.apply(OrchestratedProperty.class.getSimpleName()),
            lineageRenamer.apply("value")), valueFetcher)
        .dataFetcher(coordinates(lineageRenamer.apply(SourceProperty.class.getSimpleName()),
            lineageRenamer.apply("value")), valueFetcher)
        .dataFetcher(coordinates(lineageRenamer.apply(ObjectReference.class.getSimpleName()),
            lineageRenamer.apply("objectKey")), objectKeyFetcher);
  }

  private ObjectTypeDefinition createObjectTypeDefinition(ObjectType objectType) {
    var objectTypeDefinitionBuilder = newObjectTypeDefinition()
        .name(objectType.getName());

    objectType.getProperties(Attribute.class)
        .stream()
        .map(this::createFieldDefinition)
        .forEach(objectTypeDefinitionBuilder::fieldDefinition);

    objectType.getProperties(Relation.class)
        .stream()
        .map(this::createFieldDefinition)
        .forEach(objectTypeDefinitionBuilder::fieldDefinition);

    objectTypeDefinitionBuilder.fieldDefinition(newFieldDefinition()
        .name(lineageRenamer.apply(SchemaConstants.HAS_LINEAGE_FIELD))
        .type(requiredType(lineageRenamer.apply(ObjectLineage.class.getSimpleName())))
        .build());

    return objectTypeDefinitionBuilder.build();
  }

  private FieldDefinition createFieldDefinition(Attribute attribute) {
    return newFieldDefinition()
        .name(attribute.getName())
        .type(mapAttributeType(attribute))
        .build();
  }

  private FieldDefinition createFieldDefinition(Relation relation) {
    var target = relation.getTarget();

    return newFieldDefinition()
        .name(relation.getName())
        .type(new TypeName(target.getName()))
        .build();
  }

  private Type<?> mapAttributeType(Attribute attribute) {
    var typeName = attribute.getType()
        .getName();

    var type = switch (typeName) {
      case "Integer" -> new TypeName("Int");
      default -> new TypeName(typeName);
    };

    return REQUIRED.equals(attribute.getCardinality()) ? new NonNullType(type) : type;
  }

  private List<InputValueDefinition> createIdentityArguments(ObjectType objectType) {
    return objectType.getIdentityProperties(Attribute.class)
        .stream()
        .map(attribute -> InputValueDefinition.newInputValueDefinition()
            .name(attribute.getName())
            .type(mapAttributeType(attribute))
            .build())
        .toList();
  }
}
