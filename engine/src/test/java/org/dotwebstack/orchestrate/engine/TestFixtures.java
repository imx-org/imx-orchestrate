package org.dotwebstack.orchestrate.engine;

import java.util.Objects;
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
import org.dotwebstack.orchestrate.model.Relation;
import org.dotwebstack.orchestrate.model.combiners.Concat;
import org.dotwebstack.orchestrate.model.transforms.TestPredicate;
import org.dotwebstack.orchestrate.model.types.ScalarTypes;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TestFixtures {

  public static Model createBagModel() {
    return Model.builder()
        .objectType(ObjectType.builder()
            .name("Nummeraanduiding")
            .property(Attribute.builder()
                .name("identificatie")
                .type(ScalarTypes.STRING)
                .cardinality(Cardinality.REQUIRED)
                .identifier(true)
                .build())
            .property(Attribute.builder()
                .name("huisnummer")
                .type(ScalarTypes.INTEGER)
                .cardinality(Cardinality.REQUIRED)
                .build())
            .property(Attribute.builder()
                .name("huisnummertoevoeging")
                .type(ScalarTypes.STRING)
                .cardinality(Cardinality.OPTIONAL)
                .build())
            .property(Attribute.builder()
                .name("huisletter")
                .type(ScalarTypes.STRING)
                .cardinality(Cardinality.OPTIONAL)
                .build())
            .property(Attribute.builder()
                .name("postcode")
                .type(ScalarTypes.STRING)
                .build())
            .property(Relation.builder()
                .name("ligtAan")
                .target(ObjectTypeRef.forType("OpenbareRuimte"))
                .cardinality(Cardinality.REQUIRED)
                .build())
            .property(Relation.builder()
                .name("ligtIn")
                .target(ObjectTypeRef.forType("Woonplaats"))
                .cardinality(Cardinality.OPTIONAL)
                .build())
            .build())
        .objectType(ObjectType.builder()
            .name("OpenbareRuimte")
            .property(Attribute.builder()
                .name("identificatie")
                .type(ScalarTypes.STRING)
                .cardinality(Cardinality.REQUIRED)
                .identifier(true)
                .build())
            .property(Attribute.builder()
                .name("naam")
                .type(ScalarTypes.STRING)
                .cardinality(Cardinality.REQUIRED)
                .build())
            .property(Relation.builder()
                .name("ligtIn")
                .target(ObjectTypeRef.forType("Woonplaats"))
                .cardinality(Cardinality.REQUIRED)
                .build())
            .build())
        .objectType(ObjectType.builder()
            .name("Woonplaats")
            .property(Attribute.builder()
                .name("identificatie")
                .type(ScalarTypes.STRING)
                .cardinality(Cardinality.REQUIRED)
                .identifier(true)
                .build())
            .property(Attribute.builder()
                .name("naam")
                .type(ScalarTypes.STRING)
                .cardinality(Cardinality.REQUIRED)
                .build())
            .build())
        .objectType(ObjectType.builder()
            .name("Pand")
            .property(Attribute.builder()
                .name("identificatie")
                .type(ScalarTypes.STRING)
                .cardinality(Cardinality.REQUIRED)
                .identifier(true)
                .build())
            .property(Attribute.builder()
                .name("oorspronkelijkBouwjaar")
                .type(ScalarTypes.STRING)
                .cardinality(Cardinality.REQUIRED)
                .build())
            .property(Attribute.builder()
                .name("status")
                .type(ScalarTypes.STRING)
                .cardinality(Cardinality.REQUIRED)
                .build())
            .build())
        .objectType(ObjectType.builder()
            .name("Verblijfsobject")
            .property(Attribute.builder()
                .name("identificatie")
                .type(ScalarTypes.STRING)
                .cardinality(Cardinality.REQUIRED)
                .identifier(true)
                .build())
            .property(Relation.builder()
                .name("heeftAlsHoofdadres")
                .target(ObjectTypeRef.forType("Nummeraanduiding"))
                .cardinality(Cardinality.REQUIRED)
                .inverseName("isHoofdadresVan")
                .inverseCardinality(Cardinality.REQUIRED)
                .build())
            .property(Relation.builder()
                .name("heeftAlsNevenadres")
                .target(ObjectTypeRef.forType("Nummeraanduiding"))
                .cardinality(Cardinality.MULTI)
                .inverseName("isNevenadresVan")
                .inverseCardinality(Cardinality.OPTIONAL)
                .build())
            .property(Relation.builder()
                .name("maaktDeelUitVan")
                .target(ObjectTypeRef.forType("Pand"))
                .cardinality(Cardinality.of(1, Cardinality.INFINITE))
                .inverseName("bevat")
                .inverseCardinality(Cardinality.MULTI)
                .build())
            .build())
        .build();
  }

  public static Model createBgtModel() {
    return Model.builder()
        .objectType(ObjectType.builder()
            .name("Pand")
            .property(Attribute.builder()
                .name("identificatie")
                .type(ScalarTypes.STRING)
                .cardinality(Cardinality.REQUIRED)
                .identifier(true)
                .build())
            .property(Attribute.builder()
                .name("bgt-status")
                .type(ScalarTypes.STRING)
                .cardinality(Cardinality.REQUIRED)
                .build())
            .property(Attribute.builder()
                .name("identificatieBAGPND")
                .type(ScalarTypes.STRING)
                .cardinality(Cardinality.REQUIRED)
                .build())
            .property(Relation.builder()
                .name("isGerelateerdAan")
                .target(ObjectTypeRef.fromString("bag:Pand"))
                .cardinality(Cardinality.OPTIONAL)
                .build())
            .build())
        .build();
  }

  public static Model createTargetModel() {
    return Model.builder()
        .objectType(ObjectType.builder()
            .name("Adres")
            .property(Attribute.builder()
                .name("identificatie")
                .type(ScalarTypes.STRING)
                .cardinality(Cardinality.REQUIRED)
                .identifier(true)
                .build())
            .property(Attribute.builder()
                .name("huisnummer")
                .type(ScalarTypes.INTEGER)
                .cardinality(Cardinality.REQUIRED)
                .build())
            .property(Attribute.builder()
                .name("huisnummertoevoeging")
                .type(ScalarTypes.STRING)
                .cardinality(Cardinality.OPTIONAL)
                .build())
            .property(Attribute.builder()
                .name("huisletter")
                .type(ScalarTypes.STRING)
                .cardinality(Cardinality.OPTIONAL)
                .build())
            .property(Attribute.builder()
                .name("postcode")
                .type(ScalarTypes.STRING)
                .build())
            .property(Attribute.builder()
                .name("straatnaam")
                .type(ScalarTypes.STRING)
                .cardinality(Cardinality.REQUIRED)
                .build())
            .property(Attribute.builder()
                .name("plaatsnaam")
                .type(ScalarTypes.STRING)
                .cardinality(Cardinality.REQUIRED)
                .build())
            .property(Attribute.builder()
                .name("isHoofdadres")
                .type(ScalarTypes.BOOLEAN)
                .cardinality(Cardinality.REQUIRED)
                .build())
            .property(Attribute.builder()
                .name("omschrijving")
                .type(ScalarTypes.STRING)
                .cardinality(Cardinality.REQUIRED)
                .build())
            .build())
        .objectType(ObjectType.builder()
            .name("Gebouw")
            .property(Attribute.builder()
                .name("identificatie")
                .type(ScalarTypes.STRING)
                .cardinality(Cardinality.REQUIRED)
                .identifier(true)
                .build())
            .property(Attribute.builder()
                .name("bouwjaar")
                .type(ScalarTypes.STRING)
                .cardinality(Cardinality.OPTIONAL)
                .build())
            .property(Relation.builder()
                .name("heeftAlsAdres")
                .target(ObjectTypeRef.forType("Adres"))
                .cardinality(Cardinality.MULTI)
                .build())
            .build())
        .build();
  }

  public static ObjectTypeMapping createAdresMapping() {
    return ObjectTypeMapping.builder()
        .sourceRoot(ObjectTypeRef.fromString("bag:Nummeraanduiding"))
        .propertyMapping("identificatie", PropertyMapping.builder()
            .pathMapping(PropertyPathMapping.builder()
                .path(PropertyPath.fromString("identificatie"))
                .build())
            .build())
        .propertyMapping("huisnummer", PropertyMapping.builder()
            .pathMapping(PropertyPathMapping.builder()
                .path(PropertyPath.fromString("huisnummer"))
                .build())
            .build())
        .propertyMapping("huisnummertoevoeging", PropertyMapping.builder()
            .pathMapping(PropertyPathMapping.builder()
                .path(PropertyPath.fromString("huisnummertoevoeging"))
                .build())
            .build())
        .propertyMapping("huisletter", PropertyMapping.builder()
            .pathMapping(PropertyPathMapping.builder()
                .path(PropertyPath.fromString("huisletter"))
                .build())
            .build())
        .propertyMapping("postcode", PropertyMapping.builder()
            .pathMapping(PropertyPathMapping.builder()
                .path(PropertyPath.fromString("postcode"))
                .build())
            .build())
        .propertyMapping("straatnaam", PropertyMapping.builder()
            .pathMapping(PropertyPathMapping.builder()
                .path(PropertyPath.fromString("ligtAan/naam"))
                .build())
            .build())
        .propertyMapping("plaatsnaam", PropertyMapping.builder()
            .pathMapping(PropertyPathMapping.builder()
                .path(PropertyPath.fromString("ligtIn/naam"))
                .path(PropertyPath.fromString("ligtAan/ligtIn/naam"))
                .build())
            .build())
        .propertyMapping("isHoofdadres", PropertyMapping.builder()
            .pathMapping(PropertyPathMapping.builder()
                .path(PropertyPath.fromString("isHoofdadresVan/identificatie"))
                .transform(TestPredicate.builder()
                    .name("nonNull")
                    .predicate(Objects::nonNull)
                    .build())
                .build())
            .build())
        .propertyMapping("omschrijving", PropertyMapping.builder()
            .pathMapping(PropertyPathMapping.builder()
                .path(PropertyPath.fromString("ligtAan/naam"))
                .build())
            .pathMapping(PropertyPathMapping.builder()
                .path(PropertyPath.fromString("huisnummer"))
                .combiner(Concat.builder()
                    .prefix(" ")
                    .build())
                .build())
            .pathMapping(PropertyPathMapping.builder()
                .path(PropertyPath.fromString("huisletter"))
                .combiner(Concat.builder()
                    .prefix(" ")
                    .build())
                .build())
            .pathMapping(PropertyPathMapping.builder()
                .path(PropertyPath.fromString("huisnummertoevoeging"))
                .combiner(Concat.builder()
                    .prefix("-")
                    .build())
                .build())
            .pathMapping(PropertyPathMapping.builder()
                .path(PropertyPath.fromString("postcode"))
                .combiner(Concat.builder()
                    .prefix(", ")
                    .build())
                .build())
            .pathMapping(PropertyPathMapping.builder()
                .path(PropertyPath.fromString("ligtIn/naam"))
                .path(PropertyPath.fromString("ligtAan/ligtIn/naam"))
                .combiner(Concat.builder()
                    .prefix(" ")
                    .build())
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
        .propertyMapping("bouwjaar", PropertyMapping.builder()
            .pathMapping(PropertyPathMapping.builder()
                .path(PropertyPath.fromString("isGerelateerdAan/oorspronkelijkBouwjaar"))
                .build())
            .build())
        .propertyMapping("heeftAlsAdres", PropertyMapping.builder()
            .pathMapping(PropertyPathMapping.builder()
                // TODO: how to deal with multiple relation paths?
                // TODO: how to deal with multiple lists within a single path?
                .path(PropertyPath.fromString("isGerelateerdAan/bevat/heeftAlsHoofdadres"))
                .build())
            .build())
        .build();
  }

  public static ModelMapping createModelMapping() {
    return ModelMapping.builder()
        .targetModel(createTargetModel())
        .sourceModel("bag", createBagModel())
        .sourceModel("bgt", createBgtModel())
        .objectTypeMapping("Adres", createAdresMapping())
        .objectTypeMapping("Gebouw", createGebouwMapping())
        .build();
  }
}
