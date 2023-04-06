package org.dotwebstack.orchestrate.ext.spatial;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.dotwebstack.orchestrate.model.Attribute;
import org.dotwebstack.orchestrate.model.Cardinality;
import org.dotwebstack.orchestrate.model.Model;
import org.dotwebstack.orchestrate.model.ModelMapping;
import org.dotwebstack.orchestrate.model.ObjectType;
import org.dotwebstack.orchestrate.model.ObjectTypeMapping;
import org.dotwebstack.orchestrate.model.ObjectTypeRef;
import org.dotwebstack.orchestrate.model.PropertyMapping;
import org.dotwebstack.orchestrate.model.PropertyPath;
import org.dotwebstack.orchestrate.model.PropertyPathMapping;
import org.dotwebstack.orchestrate.model.types.ScalarTypes;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class TestFixtures {

  public static Model createTargetModel() {
    return Model.builder()
        .objectType(ObjectType.builder()
            .name("Gebouw")
            .property(Attribute.builder()
                .name("identificatie")
                .type(ScalarTypes.STRING)
                .cardinality(Cardinality.REQUIRED)
                .identifier(true)
                .build())
            .property(Attribute.builder()
                .name("bovenaanzichtgeometrie")
                .type(new GeometryType())
                .cardinality(Cardinality.REQUIRED)
                .build())
            .build())
        .build();
  }

  public static ObjectTypeMapping createGebouwMapping() {
    return ObjectTypeMapping.builder()
        .sourceRoot(ObjectTypeRef.fromString("bgt:Pand"))
        .propertyMapping("identificatie", PropertyMapping.builder()
            .pathMapping(PropertyPathMapping.builder()
                .path(PropertyPath.fromString("identificatie"))
                .build())
            .build())
        .propertyMapping("bovenaanzichtgeometrie", PropertyMapping.builder()
            .pathMapping(PropertyPathMapping.builder()
                .path(PropertyPath.fromString("geometrie2dGrondvlak"))
                .build())
            .build())
        .build();
  }

  public static Model createBgtModel() {
    return Model.builder()
        .alias("bgt")
        .objectType(ObjectType.builder()
            .name("Pand")
            .property(Attribute.builder()
                .name("identificatie")
                .type(ScalarTypes.STRING)
                .cardinality(Cardinality.REQUIRED)
                .identifier(true)
                .build())
            .property(Attribute.builder()
                .name("geometrie2dGrondvlak")
                .type(new GeometryType())
                .cardinality(Cardinality.REQUIRED)
                .build())
            .build())
        .build();
  }

  public static ModelMapping createModelMapping() {
    return ModelMapping.builder()
        .targetModel(createTargetModel())
        .sourceModel(createBgtModel())
        .objectTypeMapping("Gebouw", createGebouwMapping())
        .build();
  }
}
