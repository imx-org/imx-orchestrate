package org.dotwebstack.orchestrate.engine.fetch;

import static graphql.introspection.Introspection.INTROSPECTION_SYSTEM_FIELDS;
import static org.dotwebstack.orchestrate.engine.schema.SchemaConstants.HAS_LINEAGE_FIELD;

import graphql.schema.SelectedField;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.dotwebstack.orchestrate.engine.OrchestrateException;
import org.dotwebstack.orchestrate.model.ObjectType;
import org.dotwebstack.orchestrate.model.ObjectTypeMapping;
import org.dotwebstack.orchestrate.model.Property;
import org.dotwebstack.orchestrate.model.PropertyPath;
import org.dotwebstack.orchestrate.source.SelectedProperty;
import reactor.util.function.Tuples;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class FetchUtils {

  public static UnaryOperator<Map<String, Object>> keyExtractor(ObjectType targetType,
      ObjectTypeMapping targetMapping) {
    // TODO: Refactor & support nested keys
    var propertyMapping = targetType.getIdentityProperties()
        .stream()
        .map(property -> {
          var sourcePath = targetMapping.getPropertyMapping(property.getName())
              .getPathMappings()
              .get(0)
              .getPaths()
              .get(0)
              .getFirstSegment();

          return Tuples.of(sourcePath, property.getName());
        })
        .toList();

    return input -> propertyMapping.stream()
        .collect(HashMap::new, (acc, t) -> acc.put(t.getT1(), input.get(t.getT2())), HashMap::putAll);
  }

  public static Function<ObjectResult, Map<String, Object>> keyExtractor(ObjectType objectType) {
    return objectResult -> extractKey(objectType, objectResult.getProperties());
  }

  public static Map<String, Object> extractKey(ObjectType objectType, Map<String, Object> data) {
    return objectType.getIdentityProperties()
        .stream()
        .collect(Collectors.toMap(Property::getName, property -> data.get(property.getName())));
  }

  public static Function<ObjectResult, Map<String, Object>> propertyExtractor(String propertyName) {
    return objectResult -> cast(objectResult.getProperty(propertyName));
  }

  public static List<SelectedProperty> selectIdentity(ObjectType objectType) {
    return objectType.getIdentityProperties()
        .stream()
        .map(SelectedProperty::new)
        .toList();
  }

  public static boolean isReservedField(SelectedField selectedField, UnaryOperator<String> lineageRenamer) {
    var fieldName = selectedField.getName();
    return INTROSPECTION_SYSTEM_FIELDS.contains(fieldName) || lineageRenamer.apply(HAS_LINEAGE_FIELD).equals(fieldName);
  }

  @SuppressWarnings("unchecked")
  public static <T> T cast(Object value) {
    return (T) value;
  }

  public static <T> BinaryOperator<T> noopCombiner() {
    return (a, b) -> {
      throw new OrchestrateException("Combiner should never be called.");
    };
  }

  public static Result pathResult(Result result, PropertyPath path) {
    if (path.isLeaf()) {
      return result;
    }

    if (result instanceof ObjectResult objectResult) {
      var nestedResult = objectResult.getNestedResult(path.getFirstSegment());

      if (nestedResult == null) {
        return null;
      }

      return pathResult(nestedResult, path.withoutFirstSegment());
    }

    throw new OrchestrateException("Could not handle result for path: " + path);
  }
}
