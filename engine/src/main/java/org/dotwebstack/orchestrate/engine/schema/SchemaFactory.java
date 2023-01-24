package org.dotwebstack.orchestrate.engine.schema;

import static org.apache.commons.lang3.StringUtils.uncapitalize;
import static org.dotwebstack.orchestrate.engine.schema.SchemaConstants.QUERY_TYPE;
import static org.dotwebstack.orchestrate.model.types.Field.Cardinality.REQUIRED;

import graphql.language.FieldDefinition;
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
import java.util.Map;
import org.dotwebstack.orchestrate.model.ModelMapping;
import org.dotwebstack.orchestrate.model.types.Field;
import org.dotwebstack.orchestrate.model.types.ObjectType;
import org.dotwebstack.orchestrate.source.Source;

public final class SchemaFactory {

  public GraphQLSchema create(ModelMapping modelMapping, Map<String, Source> sourceMap) {
    var typeDefinitionRegistry = new TypeDefinitionRegistry();
    var codeRegistryBuilder = GraphQLCodeRegistry.newCodeRegistry();
    var queryTypeBuilder = ObjectTypeDefinition.newObjectTypeDefinition()
        .name(QUERY_TYPE);

    modelMapping.getTargetModel()
        .getObjectTypes()
        .stream()
        .map(this::createObjectTypeDefinition)
        .forEach(objectTypeDefinition -> {
          typeDefinitionRegistry.add(objectTypeDefinition);
          queryTypeBuilder.fieldDefinition(FieldDefinition.newFieldDefinition()
              .name(uncapitalize(objectTypeDefinition.getName()))
              .type(new TypeName(objectTypeDefinition.getName()))
              .build());
        });

    typeDefinitionRegistry.add(queryTypeBuilder.build());

    var runtimeWiring = RuntimeWiring.newRuntimeWiring()
        .codeRegistry(codeRegistryBuilder)
        .build();

    var schema = new SchemaGenerator()
        .makeExecutableSchema(typeDefinitionRegistry, runtimeWiring);

    return SchemaTransformer.transformSchema(schema, new SchemaVisitor(modelMapping, sourceMap));
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
