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
import static org.dotwebstack.orchestrate.model.Cardinality.REQUIRED;

import graphql.language.FieldDefinition;
import graphql.language.InputValueDefinition;
import graphql.language.ListType;
import graphql.language.NonNullType;
import graphql.language.ObjectTypeDefinition;
import graphql.language.Type;
import graphql.language.TypeName;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.TypeDefinitionRegistry;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.dotwebstack.orchestrate.engine.Orchestration;
import org.dotwebstack.orchestrate.engine.fetch.FetchPlanner;
import org.dotwebstack.orchestrate.engine.fetch.GenericDataFetcher;
import org.dotwebstack.orchestrate.engine.fetch.PropertyValueFetcher;
import org.dotwebstack.orchestrate.model.Attribute;
import org.dotwebstack.orchestrate.model.ModelMapping;
import org.dotwebstack.orchestrate.model.ObjectType;
import org.dotwebstack.orchestrate.model.lineage.ObjectLineage;
import org.dotwebstack.orchestrate.model.lineage.OrchestratedProperty;
import org.dotwebstack.orchestrate.model.lineage.SourceObjectReference;
import org.dotwebstack.orchestrate.model.lineage.SourceProperty;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class SchemaFactory {

  private final TypeDefinitionRegistry typeDefinitionRegistry = new TypeDefinitionRegistry();

  private final GraphQLCodeRegistry.Builder codeRegistryBuilder = newCodeRegistry();

  private final ObjectTypeDefinition.Builder queryTypeBuilder = newObjectTypeDefinition().name(QUERY_TYPE);

  private final ModelMapping modelMapping;

  private final GenericDataFetcher genericDataFetcher;

  public static GraphQLSchema create(Orchestration orchestration) {
    var fetchPlanner = new FetchPlanner(orchestration.getModelMapping(), orchestration.getSources());
    var genericDataFetcher = new GenericDataFetcher(fetchPlanner);

    return new SchemaFactory(orchestration.getModelMapping(), genericDataFetcher).create();
  }

  private GraphQLSchema create() {
    registerLineageTypes();

    modelMapping.getTargetModel()
        .getObjectTypes()
        .forEach(this::registerObjectType);

    typeDefinitionRegistry.add(queryTypeBuilder.build());

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
        .name(ObjectLineage.class.getSimpleName())
        .fieldDefinition(newFieldDefinition()
            .name("orchestratedProperties")
            .type(requiredListType(OrchestratedProperty.class))
            .build())
        .build());

    typeDefinitionRegistry.add(newObjectTypeDefinition()
        .name(OrchestratedProperty.class.getSimpleName())
        .fieldDefinition(newFieldDefinition()
            .name("property")
            .type(requiredType("String"))
            .build())
        .fieldDefinition(newFieldDefinition()
            .name("value")
            .type(requiredType("PropertyValue"))
            .build())
        .fieldDefinition(newFieldDefinition()
            .name("isDerivedFrom")
            .type(requiredListType(SourceProperty.class))
            .build())
        .build());

    typeDefinitionRegistry.add(newObjectTypeDefinition()
        .name(SourceProperty.class.getSimpleName())
        .fieldDefinition(newFieldDefinition()
            .name("subject")
            .type(requiredType(SourceObjectReference.class))
            .build())
        .fieldDefinition(newFieldDefinition()
            .name("property")
            .type(requiredType("String"))
            .build())
        .fieldDefinition(newFieldDefinition()
            .name("propertyPath")
            .type(requiredListType("String"))
            .build())
        .fieldDefinition(newFieldDefinition()
            .name("value")
            .type(requiredType("PropertyValue"))
            .build())
        .build());

    typeDefinitionRegistry.add(newObjectTypeDefinition()
        .name(SourceObjectReference.class.getSimpleName())
        .fieldDefinition(newFieldDefinition()
            .name("objectType")
            .type(requiredType("String"))
            .build())
        .fieldDefinition(newFieldDefinition()
            .name("objectKey")
            .type(requiredType("String"))
            .build())
        .build());

    typeDefinitionRegistry.add(newObjectTypeDefinition()
        .name("PropertyValue")
        .fieldDefinition(newFieldDefinition()
            .name("stringValue")
            .type(new TypeName("String"))
            .build())
        .fieldDefinition(newFieldDefinition()
            .name("integerValue")
            .type(new TypeName("Int"))
            .build())
        .fieldDefinition(newFieldDefinition()
            .name("booleanValue")
            .type(new TypeName("Boolean"))
            .build())
        .build());

    var valueFetcher = new PropertyValueFetcher();

    codeRegistryBuilder.dataFetcher(coordinates(OrchestratedProperty.class.getSimpleName(), "value"), valueFetcher)
        .dataFetcher(coordinates(SourceProperty.class.getSimpleName(), "value"), valueFetcher);
  }

  private ObjectTypeDefinition createObjectTypeDefinition(ObjectType objectType) {
    var objectTypeDefinitionBuilder = newObjectTypeDefinition()
        .name(objectType.getName());

    objectType.getProperties(Attribute.class)
        .stream()
        .map(this::createFieldDefinition)
        .forEach(objectTypeDefinitionBuilder::fieldDefinition);

    objectTypeDefinitionBuilder.fieldDefinition(newFieldDefinition()
        .name(SchemaConstants.HAS_LINEAGE_FIELD)
        .type(requiredType(ObjectLineage.class))
        .build());

    return objectTypeDefinitionBuilder.build();
  }

  private FieldDefinition createFieldDefinition(Attribute attribute) {
    return newFieldDefinition()
        .name(attribute.getName())
        .type(mapAttributeType(attribute))
        .build();
  }

  private Type<?> mapAttributeType(Attribute attribute) {
    var typeName = attribute.getType()
        .getName();

    var type = switch (typeName) {
      case "Integer" -> new TypeName("Int");
      case "String", "Boolean" -> new TypeName(typeName);
      default -> throw new RuntimeException("Type unknown: " + typeName);
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

  private static Type<?> requiredType(String typeName) {
    return new NonNullType(new TypeName(typeName));
  }

  private static Type<?> requiredType(Class<?> typeClass) {
    return requiredType(typeClass.getSimpleName());
  }

  private static Type<?> requiredListType(String typeName) {
    return new NonNullType(new ListType(requiredType(typeName)));
  }

  private static Type<?> requiredListType(Class<?> typeClass) {
    return requiredListType(typeClass.getSimpleName());
  }
}
