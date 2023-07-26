package nl.geostandaarden.imx.orchestrate.ext.spatial;

import static graphql.language.FieldDefinition.newFieldDefinition;
import static graphql.language.ObjectTypeDefinition.newObjectTypeDefinition;
import static nl.geostandaarden.imx.orchestrate.engine.schema.SchemaUtils.requiredType;
import static nl.geostandaarden.imx.orchestrate.model.types.ScalarTypes.STRING;

import graphql.language.InputObjectTypeDefinition;
import graphql.language.InputValueDefinition;
import graphql.language.TypeName;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.idl.TypeDefinitionRegistry;
import java.util.Map;
import nl.geostandaarden.imx.orchestrate.engine.OrchestrateExtension;
import nl.geostandaarden.imx.orchestrate.ext.spatial.filters.IntersectsOperatorType;
import nl.geostandaarden.imx.orchestrate.model.ComponentRegistry;
import nl.geostandaarden.imx.orchestrate.model.types.ValueTypeRegistry;

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
  public void registerComponents(ComponentRegistry componentRegistry) {
    componentRegistry.register(new IntersectsOperatorType());
  }

  @Override
  public void registerValueTypes(ValueTypeRegistry valueTypeRegistry) {
    valueTypeRegistry.register(new GeometryTypeFactory());
  }
}
