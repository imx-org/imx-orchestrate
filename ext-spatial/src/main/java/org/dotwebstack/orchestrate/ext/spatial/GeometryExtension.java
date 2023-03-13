package org.dotwebstack.orchestrate.ext.spatial;

import static graphql.language.FieldDefinition.newFieldDefinition;
import static graphql.language.ObjectTypeDefinition.newObjectTypeDefinition;
import static org.dotwebstack.orchestrate.engine.schema.SchemaUtils.requiredType;
import static org.dotwebstack.orchestrate.model.types.ScalarTypes.STRING;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.module.SimpleModule;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.idl.TypeDefinitionRegistry;
import java.util.Map;
import org.dotwebstack.orchestrate.engine.OrchestrateExtension;
import org.locationtech.jts.geom.Geometry;

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

    var geometryFetcher = new GeometryFetcher();

    codeRegistryBuilder.dataFetchers("Geometry", Map.of(
        AS_GEOJSON, geometryFetcher, AS_WKB, geometryFetcher, AS_WKT, geometryFetcher));
  }

  @Override
  public Module getLineageSerializerModule() {
    return new SimpleModule()
        .addSerializer(Geometry.class, new GeometrySerializer());
  }
}
