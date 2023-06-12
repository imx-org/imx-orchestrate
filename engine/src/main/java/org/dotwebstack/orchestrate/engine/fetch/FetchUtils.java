package org.dotwebstack.orchestrate.engine.fetch;

import static graphql.introspection.Introspection.INTROSPECTION_SYSTEM_FIELDS;
import static org.dotwebstack.orchestrate.engine.schema.SchemaConstants.HAS_LINEAGE_FIELD;

import graphql.schema.SelectedField;
import java.util.function.BinaryOperator;
import java.util.function.UnaryOperator;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.dotwebstack.orchestrate.engine.OrchestrateException;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class FetchUtils {

  public static boolean isReservedField(SelectedField selectedField, UnaryOperator<String> lineageRenamer) {
    var fieldName = selectedField.getName();
    return INTROSPECTION_SYSTEM_FIELDS.contains(fieldName) || lineageRenamer.apply(HAS_LINEAGE_FIELD).equals(fieldName);
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

  public static <T> BinaryOperator<T> noopCombiner() {
    return (a, b) -> {
      throw new OrchestrateException("Combiner should never be called.");
    };
  }
}
