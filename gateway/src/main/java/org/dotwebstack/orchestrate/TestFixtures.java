package org.dotwebstack.orchestrate;

import java.util.Objects;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.dotwebstack.orchestrate.model.Attribute;
import org.dotwebstack.orchestrate.model.Cardinality;
import org.dotwebstack.orchestrate.model.Model;
import org.dotwebstack.orchestrate.model.ModelMapping;
import org.dotwebstack.orchestrate.model.ObjectType;
import org.dotwebstack.orchestrate.model.Relation;
import org.dotwebstack.orchestrate.model.transforms.Coalesce;
import org.dotwebstack.orchestrate.model.transforms.TestPredicate;
import org.dotwebstack.orchestrate.model.transforms.TransformRegistry;
import org.dotwebstack.orchestrate.model.types.ObjectTypeRef;
import org.dotwebstack.orchestrate.model.types.ScalarTypes;
import org.dotwebstack.orchestrate.parser.yaml.YamlModelMappingParser;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TestFixtures {

  public static ModelMapping createModelMapping() {
    var targetModel = Model.builder()
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
            .build())
        .build();

    var sourceModel = Model.builder()
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
            .build())
        .build();

    var transformRegistry = TransformRegistry.builder()
        .register(Coalesce.getInstance())
        .register(TestPredicate.builder()
            .name("nonNull")
            .predicate(Objects::nonNull)
            .build())
        .build();

    var yamlMapper = YamlModelMappingParser.getInstance(transformRegistry);
    var inputStream = TestFixtures.class.getResourceAsStream("/config/adresmapping.yaml");

    var modelMapping = yamlMapper.parse(inputStream);

    return modelMapping.toBuilder()
        .targetModel(targetModel)
        .sourceModel("bag", sourceModel)
        .build();
  }
}
