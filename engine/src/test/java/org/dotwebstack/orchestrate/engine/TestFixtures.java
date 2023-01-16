package org.dotwebstack.orchestrate.engine;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.dotwebstack.orchestrate.model.FieldMapping;
import org.dotwebstack.orchestrate.model.FieldPath;
import org.dotwebstack.orchestrate.model.Model;
import org.dotwebstack.orchestrate.model.ModelMapping;
import org.dotwebstack.orchestrate.model.ObjectTypeMapping;
import org.dotwebstack.orchestrate.model.SourceTypeRef;
import org.dotwebstack.orchestrate.model.types.Field;
import org.dotwebstack.orchestrate.model.types.ObjectType;
import org.dotwebstack.orchestrate.model.types.ObjectTypeRef;
import org.dotwebstack.orchestrate.model.types.ScalarTypes;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TestFixtures {

  public static ModelMapping createModelMapping() {
    var targetModel = Model.builder()
        .objectType(ObjectType.builder()
            .name("Town")
            .field(Field.builder()
                .name("id")
                .type(ScalarTypes.INTEGER)
                .cardinality(Field.Cardinality.REQUIRED)
                .identifier(true)
                .build())
            .field(Field.builder()
                .name("name")
                .type(ScalarTypes.STRING)
                .build())
            .field(Field.builder()
                .name("municipality")
                .type(ScalarTypes.STRING)
                .build())
            .field(Field.builder()
                .name("province")
                .type(ScalarTypes.STRING)
                .build())
            .build())
        .build();

    var sourceModel = Model.builder()
        .objectType(ObjectType.builder()
            .name("Municipality")
            .field(Field.builder()
                .name("id")
                .type(ScalarTypes.INTEGER)
                .cardinality(Field.Cardinality.REQUIRED)
                .identifier(true)
                .build())
            .field(Field.builder()
                .name("name")
                .type(ScalarTypes.STRING)
                .build())
            .field(Field.builder()
                .name("province")
                .type(ObjectTypeRef.forType("Province"))
                .build())
            .build())
        .objectType(ObjectType.builder()
            .name("Province")
            .field(Field.builder()
                .name("id")
                .type(ScalarTypes.INTEGER)
                .cardinality(Field.Cardinality.REQUIRED)
                .identifier(true)
                .build())
            .field(Field.builder()
                .name("name")
                .type(ScalarTypes.STRING)
                .build())
            .build())
        .objectType(ObjectType.builder()
            .name("City")
            .field(Field.builder()
                .name("id")
                .type(ScalarTypes.INTEGER)
                .cardinality(Field.Cardinality.REQUIRED)
                .identifier(true)
                .build())
            .field(Field.builder()
                .name("name")
                .type(ScalarTypes.STRING)
                .cardinality(Field.Cardinality.REQUIRED)
                .build())
            .field(Field.builder()
                .name("municipality")
                .type(ObjectTypeRef.forType("Municipality"))
                .build())
            .build())
        .build();

    return ModelMapping.builder()
        .targetModel(targetModel)
        .sourceModel("src", sourceModel)
        .objectTypeMapping("Town", ObjectTypeMapping.builder()
            .sourceRoot(SourceTypeRef.fromString("src:City"))
            .fieldMapping("id", FieldMapping.builder()
                .sourcePath(FieldPath.fromString("id"))
                .build())
            .fieldMapping("name", FieldMapping.builder()
                .sourcePath(FieldPath.fromString("name"))
                .build())
            .fieldMapping("municipality", FieldMapping.builder()
                .sourcePath(FieldPath.fromString("municipality/name"))
                .build())
            .fieldMapping("province", FieldMapping.builder()
                .sourcePath(FieldPath.fromString("municipality/province/name"))
                .build())
            .build())
        .build();
  }
}
