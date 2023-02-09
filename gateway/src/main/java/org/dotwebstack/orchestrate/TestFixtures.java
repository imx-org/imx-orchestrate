package org.dotwebstack.orchestrate;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.dotwebstack.orchestrate.model.Attribute;
import org.dotwebstack.orchestrate.model.Cardinality;
import org.dotwebstack.orchestrate.model.ComponentRegistry;
import org.dotwebstack.orchestrate.model.Model;
import org.dotwebstack.orchestrate.model.ModelMapping;
import org.dotwebstack.orchestrate.model.ObjectType;
import org.dotwebstack.orchestrate.model.Relation;
import org.dotwebstack.orchestrate.model.combiners.Concat;
import org.dotwebstack.orchestrate.model.transforms.TestPredicate;
import org.dotwebstack.orchestrate.model.types.ObjectTypeRef;
import org.dotwebstack.orchestrate.model.types.ScalarTypes;
import org.dotwebstack.orchestrate.parser.yaml.YamlModelMappingParser;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class TestFixtures {

  static final Map<String, Map<String, Object>> NUM_DATA = new LinkedHashMap<>();

  static final Map<String, Map<String, Object>> VBO_DATA = new LinkedHashMap<>();

  static final Map<String, Map<String, Object>> OPR_DATA = new LinkedHashMap<>();

  static final Map<String, Map<String, Object>> WPL_DATA = new LinkedHashMap<>();

  static {
    NUM_DATA.put("0200200000075716", Map.of("identificatie", "0200200000075716", "huisnummer", 701, "postcode", "7334DP",
        "ligtAan", Map.of("identificatie", "0200300022472362")));
    NUM_DATA.put("0200200000075717", Map.of("identificatie", "0200200000075717", "huisnummer", 702, "postcode", "7334DP",
        "ligtAan", Map.of("identificatie", "0200300022472362")));
    NUM_DATA.put("0200200000075718", Map.of("identificatie", "0200200000075718", "huisnummer", 703, "huisnummertoevoeging", "8", "huisletter", "C", "postcode", "7334DP",
        "ligtAan", Map.of("identificatie", "0200300022472362"), "ligtIn", Map.of("identificatie", "2258")));
    VBO_DATA.put("0200200000075716", Map.of("identificatie", "0200010000130331"));
    VBO_DATA.put("0200200000075718", Map.of("identificatie", "0200010000130331"));
    OPR_DATA.put("0200300022472362", Map.of("identificatie", "0200300022472362", "naam", "Laan van Westenenk", "ligtIn", Map.of("identificatie", "3560")));
    WPL_DATA.put("3560", Map.of("identificatie", "3560", "naam", "Apeldoorn"));
    WPL_DATA.put("2258", Map.of("identificatie", "2258", "naam", "Beekbergen"));
  }

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

    var componentRegistry = new ComponentRegistry()
        .registerTransform(TestPredicate.builder()
            .name("nonNull")
            .predicate(Objects::nonNull)
            .build());

    var yamlMapper = YamlModelMappingParser.getInstance(Map.of("concat", Concat.class, "nonNull", TestPredicate.class),
        componentRegistry);
    var inputStream = TestFixtures.class.getResourceAsStream("/config/adresmapping.yaml");

    var modelMapping = yamlMapper.parse(inputStream);

    return modelMapping.toBuilder()
        .targetModel(targetModel)
        .sourceModel("bag", sourceModel)
        .build();
  }
}
