package nl.geostandaarden.imx.orchestrate.engine.fetch;

import static graphql.introspection.Introspection.INTROSPECTION_SYSTEM_FIELDS;

import graphql.schema.SelectedField;
import java.util.Map;
import java.util.function.UnaryOperator;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import nl.geostandaarden.imx.orchestrate.engine.OrchestrateException;
import nl.geostandaarden.imx.orchestrate.engine.schema.SchemaConstants;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class FetchUtils {

  public static boolean isReservedField(SelectedField selectedField, UnaryOperator<String> lineageRenamer) {
    var fieldName = selectedField.getName();
    return INTROSPECTION_SYSTEM_FIELDS.contains(fieldName) || lineageRenamer.apply(SchemaConstants.HAS_LINEAGE_FIELD).equals(fieldName);
  }

  @SuppressWarnings("unchecked")
  public static <T> T cast(Object value) {
    return (T) value;
  }

  public static <T> T cast(Object value, Class<T> clazz) {
    if (!clazz.isInstance(value)) {
      throw new OrchestrateException("Could not cast value to class: " + clazz.getSimpleName());
    }

    return clazz.cast(value);
  }

  public static Map<String, Object> castToMap(Object value) {
    return cast(value);
  }
}