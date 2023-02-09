package org.dotwebstack.orchestrate.engine.fetch;

import static graphql.introspection.Introspection.INTROSPECTION_SYSTEM_FIELDS;
import static org.dotwebstack.orchestrate.engine.schema.SchemaConstants.HAS_LINEAGE_FIELD;

import graphql.schema.SelectedField;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.dotwebstack.orchestrate.engine.OrchestrateException;
import org.dotwebstack.orchestrate.model.ObjectType;
import org.dotwebstack.orchestrate.model.Property;
import org.dotwebstack.orchestrate.model.PropertyPath;
import org.dotwebstack.orchestrate.source.SelectedProperty;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class FetchUtils {

  public static UnaryOperator<Map<String, Object>> inputMapper(String propertyName) {
    return input -> Optional.ofNullable(input.get(propertyName))
        .map(FetchUtils::<Map<String, Object>>cast)
        .orElse(null);
  }

  public static UnaryOperator<Map<String, Object>> keyExtractor(ObjectType objectType) {
    return data -> objectType.getIdentityProperties()
        .stream()
        .collect(Collectors.toMap(Property::getName, property -> data.get(property.getName())));
  }

  public static List<SelectedProperty> selectIdentity(ObjectType objectType) {
    return objectType.getIdentityProperties()
        .stream()
        .map(SelectedProperty::new)
        .toList();
  }

  public static boolean isReservedField(SelectedField selectedField) {
    var fieldName = selectedField.getName();
    return INTROSPECTION_SYSTEM_FIELDS.contains(fieldName) || HAS_LINEAGE_FIELD.equals(fieldName);
  }

  @SuppressWarnings("unchecked")
  public static <T> T cast(Object value) {
    return (T) value;
  }

  public static BinaryOperator<Object> noopCombiner() {
    return (a, b) -> {
      throw new OrchestrateException("Combiner should never be called.");
    };
  }

  public static Object pathValue(Map<String, Object> data, PropertyPath path) {
    if (path.isLeaf()) {
      return data.get(path.getFirstSegment());
    }

    Map<String, Object> nestedData = cast(data.get(path.getFirstSegment()));

    if (nestedData == null) {
      return null;
    }

    return pathValue(nestedData, path.withoutFirstSegment());
  }
}
