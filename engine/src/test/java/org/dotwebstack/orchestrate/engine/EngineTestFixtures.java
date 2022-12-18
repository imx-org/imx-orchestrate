package org.dotwebstack.orchestrate.engine;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.dotwebstack.orchestrate.model.FieldMapping;
import org.dotwebstack.orchestrate.model.FieldPath;
import org.dotwebstack.orchestrate.model.Model;
import org.dotwebstack.orchestrate.model.ModelMapping;
import org.dotwebstack.orchestrate.model.ObjectTypeMapping;
import org.dotwebstack.orchestrate.model.SourceRoot;
import org.dotwebstack.orchestrate.model.types.Field;
import org.dotwebstack.orchestrate.model.types.ObjectType;
import org.dotwebstack.orchestrate.model.types.ObjectTypeRef;
import org.dotwebstack.orchestrate.model.types.ScalarTypes;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EngineTestFixtures {

  public static ModelMapping createModelMapping() {
    var targetModel = Model.builder()
        .objectType(ObjectType.builder()
            .name("Area")
            .field(Field.builder()
                .name("code")
                .type(ScalarTypes.INTEGER)
                .cardinality(Field.Cardinality.REQUIRED)
                .identifier(true)
                .build())
            .field(Field.builder()
                .name("name")
                .type(ScalarTypes.STRING)
                .build())
            .build())
        .build();

    var sourceModel = Model.builder()
        .objectType(ObjectType.builder()
            .name("Person")
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
                .name("mayor")
                .type(ObjectTypeRef.forType("Person"))
                .build())
            .build())
        .build();

    return ModelMapping.builder()
        .targetModel(targetModel)
        .sourceModel("src", sourceModel)
        .objectTypeMapping("Area", ObjectTypeMapping.builder()
            .sourceRoot(SourceRoot.fromString("src:City"))
            .fieldMapping("code", FieldMapping.builder()
                .sourcePath(FieldPath.fromString("id"))
                .build())
            .fieldMapping("manager", FieldMapping.builder()
                .sourcePath(FieldPath.fromString("mayor/name"))
                .build())
            .build())
        .build();
  }
}
