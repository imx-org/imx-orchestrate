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
            .name("Adres")
            .field(Field.builder()
                .name("identificatie")
                .type(ScalarTypes.STRING)
                .cardinality(Field.Cardinality.REQUIRED)
                .identifier(true)
                .build())
            .field(Field.builder()
                .name("huisnummer")
                .type(ScalarTypes.INTEGER)
                .cardinality(Field.Cardinality.REQUIRED)
                .build())
            .field(Field.builder()
                .name("postcode")
                .type(ScalarTypes.STRING)
                .build())
            .field(Field.builder()
                .name("straatnaam")
                .type(ScalarTypes.STRING)
                .cardinality(Field.Cardinality.REQUIRED)
                .build())
            .field(Field.builder()
                .name("plaatsnaam")
                .type(ScalarTypes.STRING)
                .cardinality(Field.Cardinality.REQUIRED)
                .build())
            .build())
        .build();

    var sourceModel = Model.builder()
        .objectType(ObjectType.builder()
            .name("Nummeraanduiding")
            .field(Field.builder()
                .name("identificatie")
                .type(ScalarTypes.STRING)
                .cardinality(Field.Cardinality.REQUIRED)
                .identifier(true)
                .build())
            .field(Field.builder()
                .name("huisnummer")
                .type(ScalarTypes.INTEGER)
                .cardinality(Field.Cardinality.REQUIRED)
                .build())
            .field(Field.builder()
                .name("postcode")
                .type(ScalarTypes.STRING)
                .build())
            .field(Field.builder()
                .name("ligtAan")
                .type(ObjectTypeRef.forType("OpenbareRuimte"))
                .cardinality(Field.Cardinality.REQUIRED)
                .build())
            .build())
        .objectType(ObjectType.builder()
            .name("OpenbareRuimte")
            .field(Field.builder()
                .name("identificatie")
                .type(ScalarTypes.STRING)
                .cardinality(Field.Cardinality.REQUIRED)
                .identifier(true)
                .build())
            .field(Field.builder()
                .name("naam")
                .type(ScalarTypes.STRING)
                .cardinality(Field.Cardinality.REQUIRED)
                .build())
            .field(Field.builder()
                .name("ligtIn")
                .type(ObjectTypeRef.forType("Woonplaats"))
                .cardinality(Field.Cardinality.REQUIRED)
                .build())
            .build())
        .objectType(ObjectType.builder()
            .name("Woonplaats")
            .field(Field.builder()
                .name("identificatie")
                .type(ScalarTypes.STRING)
                .cardinality(Field.Cardinality.REQUIRED)
                .identifier(true)
                .build())
            .field(Field.builder()
                .name("naam")
                .type(ScalarTypes.STRING)
                .cardinality(Field.Cardinality.REQUIRED)
                .build())
            .build())
        .build();

    var adresMapping = ObjectTypeMapping.builder()
        .sourceRoot(SourceTypeRef.fromString("bag:Nummeraanduiding"))
        .fieldMapping("identificatie", FieldMapping.builder()
            .sourcePath(FieldPath.fromString("identificatie"))
            .build())
        .fieldMapping("huisnummer", FieldMapping.builder()
            .sourcePath(FieldPath.fromString("huisnummer"))
            .build())
        .fieldMapping("postcode", FieldMapping.builder()
            .sourcePath(FieldPath.fromString("postcode"))
            .build())
        .fieldMapping("straatnaam", FieldMapping.builder()
            .sourcePath(FieldPath.fromString("ligtAan/naam"))
            .build())
        .fieldMapping("plaatsnaam", FieldMapping.builder()
            .sourcePath(FieldPath.fromString("ligtAan/ligtIn/naam"))
            .build())
        .build();

    return ModelMapping.builder()
        .targetModel(targetModel)
        .sourceModel("bag", sourceModel)
        .objectTypeMapping("Adres", adresMapping)
        .build();
  }
}
