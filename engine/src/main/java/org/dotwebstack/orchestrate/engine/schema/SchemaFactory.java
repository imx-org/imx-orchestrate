package org.dotwebstack.orchestrate.engine.schema;

import static org.apache.commons.lang3.StringUtils.uncapitalize;
import static org.dotwebstack.orchestrate.engine.schema.SchemaConstants.QUERY_TYPE;
import static org.dotwebstack.orchestrate.model.types.Field.Cardinality.REQUIRED;

import graphql.language.FieldDefinition;
import graphql.language.InputValueDefinition;
import graphql.language.NonNullType;
import graphql.language.ObjectTypeDefinition;
import graphql.language.Type;
import graphql.language.TypeName;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLSchema;
import graphql.schema.SchemaTransformer;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.TypeDefinitionRegistry;
import java.util.List;
import org.dotwebstack.orchestrate.engine.Orchestration;
import org.dotwebstack.orchestrate.engine.fetch.FetchPlanner;
import org.dotwebstack.orchestrate.engine.fetch.ObjectFetcher;
import org.dotwebstack.orchestrate.model.types.Field;
import org.dotwebstack.orchestrate.model.types.ObjectType;

public final class SchemaFactory {

  public GraphQLSchema create(Orchestration orchestration) {
    var typeDefinitionRegistry = new TypeDefinitionRegistry();
    var codeRegistryBuilder = GraphQLCodeRegistry.newCodeRegistry();
    var queryTypeBuilder = ObjectTypeDefinition.newObjectTypeDefinition()
        .name(QUERY_TYPE);

    orchestration.getModelMapping()
        .getTargetModel()
        .getObjectTypes()
        .forEach(objectType -> {
          var objectTypeDefinition = createObjectTypeDefinition(objectType);
          typeDefinitionRegistry.add(objectTypeDefinition);
          queryTypeBuilder.fieldDefinition(FieldDefinition.newFieldDefinition()
              .name(uncapitalize(objectTypeDefinition.getName()))
              .type(new TypeName(objectTypeDefinition.getName()))
              .inputValueDefinitions(createIdentityArguments(objectType))
              .build());
        });

    typeDefinitionRegistry.add(queryTypeBuilder.build());

    var runtimeWiring = RuntimeWiring.newRuntimeWiring()
        .codeRegistry(codeRegistryBuilder)
        .build();

    var schema = new SchemaGenerator()
        .makeExecutableSchema(typeDefinitionRegistry, runtimeWiring);

    var fetchPlanner = new FetchPlanner(orchestration.getModelMapping(), orchestration.getSources());

    return SchemaTransformer.transformSchema(schema, new SchemaVisitor(orchestration.getModelMapping(),
        new ObjectFetcher(fetchPlanner)));
  }

  private List<InputValueDefinition> createIdentityArguments(ObjectType objectType) {
    return objectType.getIdentityFields()
        .stream()
        .map(field -> InputValueDefinition.newInputValueDefinition()
            .name(field.getName())
            .type(mapFieldType(field))
            .build())
        .toList();
  }

  private ObjectTypeDefinition createObjectTypeDefinition(ObjectType objectType) {
    var objectTypeDefinitionBuilder = ObjectTypeDefinition.newObjectTypeDefinition()
        .name(objectType.getName());

    objectType.getFields()
        .stream()
        .map(this::createFieldDefinition)
        .forEach(objectTypeDefinitionBuilder::fieldDefinition);

    return objectTypeDefinitionBuilder.build();
  }

  private FieldDefinition createFieldDefinition(Field field) {
    return FieldDefinition.newFieldDefinition()
        .name(field.getName())
        .type(mapFieldType(field))
        .build();
  }

  private Type<?> mapFieldType(Field field) {
    var typeName = field.getType()
        .getName();

    var type = switch (typeName) {
      case "Integer" -> new TypeName("Int");
      case "String" -> new TypeName(typeName);
      default -> throw new RuntimeException("Type unknown: " + typeName);
    };

    return REQUIRED.equals(field.getCardinality()) ? new NonNullType(type) : type;
  }
}
