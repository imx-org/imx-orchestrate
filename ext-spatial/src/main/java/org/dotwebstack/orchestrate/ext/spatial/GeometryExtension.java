package org.dotwebstack.orchestrate.ext.spatial;

import static graphql.language.FieldDefinition.newFieldDefinition;
import static graphql.language.ObjectTypeDefinition.newObjectTypeDefinition;
import static org.dotwebstack.orchestrate.engine.schema.SchemaUtils.requiredType;
import static org.dotwebstack.orchestrate.model.types.ScalarTypes.STRING;

import graphql.language.InputObjectTypeDefinition;
import graphql.language.InputValueDefinition;
import graphql.language.TypeName;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.idl.TypeDefinitionRegistry;
import java.util.Map;
import org.dotwebstack.orchestrate.engine.OrchestrateExtension;
import org.dotwebstack.orchestrate.ext.spatial.filters.IntersectsOperatorType;
import org.dotwebstack.orchestrate.model.ComponentFactory;

public final class GeometryExtension implements OrchestrateExtension {

  public static final String AS_GEOJSON = "asGeoJSON";

  public static final String AS_WKB = "asWKB";

  public static final String AS_WKT = "asWKT";

  @Override
  public void enhanceSchema(TypeDefinitionRegistry typeDefinitionRegistry,
      GraphQLCodeRegistry.Builder codeRegistryBuilder) {
    typeDefinitionRegistry.add(newObjectTypeDefinition()
        .name("Geometry")
        .fieldDefinition(newFieldDefinition()
            .name(AS_GEOJSON)
            .type(requiredType(STRING.getName()))
            .build())
        .fieldDefinition(newFieldDefinition()
            .name(AS_WKB)
            .type(requiredType(STRING.getName()))
            .build())
        .fieldDefinition(newFieldDefinition()
            .name(AS_WKT)
            .type(requiredType(STRING.getName()))
            .build())
        .build());

    typeDefinitionRegistry.add(InputObjectTypeDefinition.newInputObjectDefinition()
        .name("GeometryFilter")
        .inputValueDefinition(InputValueDefinition.newInputValueDefinition()
            .name("intersects")
            .type(new TypeName("GeometryInput"))
            .build())
        .build());

    typeDefinitionRegistry.add(InputObjectTypeDefinition.newInputObjectDefinition()
        .name("GeometryInput")
        .inputValueDefinition(InputValueDefinition.newInputValueDefinition()
            .name("fromWKT")
            .type(new TypeName("String"))
            .build())
        .build());

    var geometryFetcher = new GeometryFetcher();

    codeRegistryBuilder.dataFetchers("Geometry", Map.of(
        AS_GEOJSON, geometryFetcher, AS_WKB, geometryFetcher, AS_WKT, geometryFetcher));
  }

  @Override
  public void registerComponents(ComponentFactory componentFactory) {
    componentFactory.register(new IntersectsOperatorType());
  }
}
