package nl.geostandaarden.imx.orchestrate.engine.schema;

import static graphql.language.FieldDefinition.newFieldDefinition;
import static graphql.language.ObjectTypeDefinition.newObjectTypeDefinition;
import static graphql.schema.FieldCoordinates.coordinates;
import static graphql.schema.GraphQLCodeRegistry.newCodeRegistry;
import static graphql.schema.SchemaTransformer.transformSchema;
import static graphql.schema.idl.RuntimeWiring.newRuntimeWiring;
import static org.apache.commons.lang3.StringUtils.uncapitalize;
import static nl.geostandaarden.imx.orchestrate.engine.schema.SchemaUtils.applyCardinality;
import static nl.geostandaarden.imx.orchestrate.engine.schema.SchemaUtils.requiredListType;
import static nl.geostandaarden.imx.orchestrate.engine.schema.SchemaUtils.requiredType;

import graphql.language.InputObjectTypeDefinition;
import graphql.language.InputValueDefinition;
import graphql.language.ObjectTypeDefinition;
import graphql.language.Type;
import graphql.language.TypeName;
import graphql.schema.FieldCoordinates;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.TypeDefinitionRegistry;
import java.util.List;
import java.util.Set;
import java.util.function.UnaryOperator;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import nl.geostandaarden.imx.orchestrate.engine.OrchestrateException;
import nl.geostandaarden.imx.orchestrate.engine.OrchestrateExtension;
import nl.geostandaarden.imx.orchestrate.engine.fetch.FetchPlanner;
import nl.geostandaarden.imx.orchestrate.engine.Orchestration;
import nl.geostandaarden.imx.orchestrate.engine.fetch.GenericDataFetcher;
import nl.geostandaarden.imx.orchestrate.engine.fetch.ObjectKeyFetcher;
import nl.geostandaarden.imx.orchestrate.engine.fetch.ObjectLineageFetcher;
import nl.geostandaarden.imx.orchestrate.engine.fetch.PropertyValueFetcher;
import nl.geostandaarden.imx.orchestrate.model.AbstractRelation;
import nl.geostandaarden.imx.orchestrate.model.Attribute;
import nl.geostandaarden.imx.orchestrate.model.ModelMapping;
import nl.geostandaarden.imx.orchestrate.model.ObjectType;
import nl.geostandaarden.imx.orchestrate.model.Property;
import nl.geostandaarden.imx.orchestrate.model.lineage.ObjectLineage;
import nl.geostandaarden.imx.orchestrate.model.lineage.ObjectReference;
import nl.geostandaarden.imx.orchestrate.model.lineage.OrchestratedProperty;
import nl.geostandaarden.imx.orchestrate.model.lineage.Path;
import nl.geostandaarden.imx.orchestrate.model.lineage.PathMapping;
import nl.geostandaarden.imx.orchestrate.model.lineage.PropertyMapping;
import nl.geostandaarden.imx.orchestrate.model.lineage.PropertyMappingExecution;
import nl.geostandaarden.imx.orchestrate.model.lineage.SourceProperty;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class SchemaFactory {

  private final TypeDefinitionRegistry typeDefinitionRegistry = new TypeDefinitionRegistry();

  private final GraphQLCodeRegistry.Builder codeRegistryBuilder = newCodeRegistry();

  private final ObjectTypeDefinition.Builder queryTypeBuilder = newObjectTypeDefinition().name(SchemaConstants.QUERY_TYPE);

  private final ModelMapping modelMapping;

  private final GenericDataFetcher genericDataFetcher;

  private final ObjectLineageFetcher objectLineageFetcher;

  private final UnaryOperator<String> lineageRenamer;

  private final Set<OrchestrateExtension> extensions;

  public static GraphQLSchema create(Orchestration orchestration) {
    var modelMapping = orchestration.getModelMapping();

    UnaryOperator<String> lineageRenamer =
        fieldName -> modelMapping.getLineageNameMapping().getOrDefault(fieldName, fieldName);

    var fetchPlanner = new FetchPlanner(modelMapping, orchestration.getSources(), lineageRenamer);
    var genericDataFetcher = new GenericDataFetcher(fetchPlanner, modelMapping.getTargetModel());
    var objectLineageFetcher = new ObjectLineageFetcher(modelMapping.getLineageNameMapping());

    return new SchemaFactory(modelMapping, genericDataFetcher, objectLineageFetcher, lineageRenamer,
        orchestration.getExtensions()).create();
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
    if (!modelMapping.getObjectTypeMappings()
        .containsKey(objectType.getName())) {
      return;
    }

    var objectTypeDefinition = createObjectTypeDefinition(objectType);
    typeDefinitionRegistry.add(objectTypeDefinition);

    var baseName = uncapitalize(objectTypeDefinition.getName());
    var collectionName = baseName.concat(SchemaConstants.QUERY_COLLECTION_SUFFIX);

    var objectField = newFieldDefinition()
        .name(baseName)
        .type(new TypeName(objectTypeDefinition.getName()))
        .inputValueDefinitions(createIdentityArguments(objectType))
        .build();

    var collectionField = newFieldDefinition()
        .name(collectionName)
        .type(requiredListType(objectTypeDefinition.getName()))
        .build();

    var filterFields = objectType.getProperties(Attribute.class)
        .stream()
        .filter(attribute -> isFilterable(objectType, attribute))
        .map(attribute -> {
          var typeName = attribute.getType()
              .getName();

          // TODO: Decouple Geometry type handling
          return InputValueDefinition.newInputValueDefinition()
              .name(attribute.getName())
              .type(typeName.equals("Geometry") ? new TypeName("GeometryFilter") : mapFieldType(attribute, false))
              .build();
        })
        .toList();

    if (!filterFields.isEmpty()) {
      typeDefinitionRegistry.add(InputObjectTypeDefinition.newInputObjectDefinition()
          .name(objectType.getName()
              .concat(SchemaConstants.QUERY_FILTER_SUFFIX))
          .inputValueDefinitions(filterFields)
          .build());

      var filterArgument = InputValueDefinition.newInputValueDefinition()
          .name(SchemaConstants.QUERY_FILTER_ARGUMENTS)
          .type(new TypeName(objectType.getName()
              .concat(SchemaConstants.QUERY_FILTER_SUFFIX)))
          .build();

      collectionField = collectionField.transform(builder -> builder.inputValueDefinition(filterArgument));
    }

    queryTypeBuilder.fieldDefinition(objectField)
        .fieldDefinition(collectionField);

    codeRegistryBuilder.dataFetcher(FieldCoordinates.coordinates(SchemaConstants.QUERY_TYPE, baseName), genericDataFetcher)
        .dataFetcher(FieldCoordinates.coordinates(SchemaConstants.QUERY_TYPE, collectionName), genericDataFetcher);
  }

  private boolean isFilterable(ObjectType objectType, Attribute attribute) {
    if (attribute.isIdentifier()) {
      return false;
    }

    var pathMappings = modelMapping.getObjectTypeMapping(objectType)
        .getPropertyMapping(attribute)
        .getPathMappings();

    var firstPath = pathMappings.get(0)
        .getPath();

    return pathMappings.size() == 1 && firstPath.isLeaf();
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
            .type(new TypeName("PropertyResult"))
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
            .type(requiredListType(lineageRenamer.apply(PathMapping.class.getSimpleName())))
            .build())
        .build());

    typeDefinitionRegistry.add(newObjectTypeDefinition()
        .name(lineageRenamer.apply(PathMapping.class.getSimpleName()))
        .fieldDefinition(newFieldDefinition()
            .name(lineageRenamer.apply("path"))
            .type(requiredListType(lineageRenamer.apply(Path.class.getSimpleName())))
            .build())
        .build());

    typeDefinitionRegistry.add(newObjectTypeDefinition()
        .name(lineageRenamer.apply(Path.class.getSimpleName()))
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
            .name(lineageRenamer.apply("path"))
            .type(requiredListType("String"))
            .build())
        .fieldDefinition(newFieldDefinition()
            .name(lineageRenamer.apply("value"))
            .type(new TypeName(lineageRenamer.apply("PropertyResult")))
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
        .name(lineageRenamer.apply("PropertyResult"))
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
        .fieldDefinition(newFieldDefinition()
            .name(lineageRenamer.apply("objectValue"))
            .type(new TypeName(lineageRenamer.apply(ObjectReference.class.getSimpleName())))
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

    objectType.getProperties()
        .stream()
        .map(property -> newFieldDefinition()
            .name(property.getName())
            .type(mapFieldType(property))
            .build())
        .forEach(objectTypeDefinitionBuilder::fieldDefinition);

    var lineageFieldName = lineageRenamer.apply(SchemaConstants.HAS_LINEAGE_FIELD);

    objectTypeDefinitionBuilder.fieldDefinition(newFieldDefinition()
        .name(lineageFieldName)
        .type(requiredType(lineageRenamer.apply(ObjectLineage.class.getSimpleName())))
        .build());

    codeRegistryBuilder.dataFetcher(coordinates(objectType.getName(), lineageFieldName), objectLineageFetcher);

    return objectTypeDefinitionBuilder.build();
  }

  private Type<?> mapFieldType(Property property) {
    if (property instanceof Attribute attribute) {
      return mapFieldType(attribute, true);
    }

    if (property instanceof AbstractRelation relation) {
      var target = relation.getTarget();
      return applyCardinality(new TypeName(target.getName()), relation.getCardinality());
    }

    throw new OrchestrateException("Could not map field type");
  }

  private Type<?> mapFieldType(Attribute attribute, boolean applyCardinality) {
    var typeName = attribute.getType()
        .getName();

    var type = switch (typeName) {
      case "Integer" -> new TypeName("Int");
      case "Double" -> new TypeName("Float");
      default -> new TypeName(typeName);
    };

    return applyCardinality ? applyCardinality(type, attribute.getCardinality()) : type;
  }

  private List<InputValueDefinition> createIdentityArguments(ObjectType objectType) {
    return objectType.getIdentityProperties(Attribute.class)
        .stream()
        .map(attribute -> InputValueDefinition.newInputValueDefinition()
            .name(attribute.getName())
            .type(mapFieldType(attribute, true))
            .build())
        .toList();
  }
}
