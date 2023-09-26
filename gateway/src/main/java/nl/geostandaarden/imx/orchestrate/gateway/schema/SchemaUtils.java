package nl.geostandaarden.imx.orchestrate.gateway.schema;

import static graphql.introspection.Introspection.INTROSPECTION_SYSTEM_FIELDS;
import static nl.geostandaarden.imx.orchestrate.model.Cardinality.REQUIRED;

import graphql.language.ListType;
import graphql.language.NonNullType;
import graphql.language.Type;
import graphql.language.TypeName;
import graphql.schema.SelectedField;
import java.util.function.UnaryOperator;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import nl.geostandaarden.imx.orchestrate.model.Cardinality;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SchemaUtils {

  public static Type<?> requiredType(String typeName) {
    return new NonNullType(new TypeName(typeName));
  }

  public static Type<?> requiredListType(String typeName) {
    return new NonNullType(new ListType(requiredType(typeName)));
  }

  public static Type<?> applyCardinality(Type<?> type, Cardinality cardinality) {
    if (!cardinality.isSingular()) {
      return new ListType(new NonNullType(type));
    }

    if (cardinality.equals(REQUIRED)) {
      return new NonNullType(type);
    }

    return type;
  }

  public static boolean isReservedField(SelectedField selectedField, UnaryOperator<String> lineageRenamer) {
    var fieldName = selectedField.getName();
    return INTROSPECTION_SYSTEM_FIELDS.contains(fieldName); // || lineageRenamer.apply(SchemaConstants.HAS_LINEAGE_FIELD).equals(fieldName);
  }
}
