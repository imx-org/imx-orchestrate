package nl.geostandaarden.imx.orchestrate.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ModelUtils {

  public static UnaryOperator<Map<String, Object>> keyExtractor(ObjectType targetType,
      ObjectTypeMapping targetMapping) {
    // TODO: Refactor & support nested keys
    var propertyMapping = targetType.getIdentityProperties()
        .stream()
        .collect(Collectors.toMap(property -> targetMapping.getPropertyMapping(property.getName())
            .getPathMappings()
            .get(0)
            .getPath()
            .getFirstSegment(), Property::getName));

    return input -> propertyMapping.entrySet()
        .stream()
        .collect(HashMap::new, (acc, e) -> acc.put(e.getKey(), input.get(e.getValue())), HashMap::putAll);
  }

  public static Function<ObjectResult, Map<String, Object>> keyExtractor(ObjectType objectType) {
    return objectResult -> extractKey(objectType, objectResult.getProperties());
  }

  public static Map<String, Object> extractKey(ObjectType objectType, Map<String, Object> data) {
    return objectType.getIdentityProperties()
        .stream()
        .collect(Collectors.toMap(Property::getName, property -> data.get(property.getName())));
  }

  public static Map<String, Object> extractKey(ObjectResult objectResult, Map<String, Path> keyMapping) {
    return keyMapping.entrySet()
        .stream()
        .collect(Collectors.toMap(Map.Entry::getKey, entry -> {
          var keyPath = entry.getValue();

          if (!keyPath.isLeaf()) {
            throw new ModelException("Only leaf paths are (currently) supported: " + keyPath);
          }

          var objectType = objectResult.getType();
          var propertyName = keyPath.getFirstSegment();

          if (!(objectType.getProperty(propertyName) instanceof Attribute)) {
            throw new ModelException("Only attribute keys are (currently) supported: " + propertyName);
          }

          return Optional.ofNullable(objectResult.getProperty(propertyName))
              .orElseThrow(() -> new ModelException("Key properties may never be null."));
        }));
  }

  public static <T> BinaryOperator<T> noopCombiner() {
    return (a, b) -> {
      throw new IllegalStateException("Combiner should never be called.");
    };
  }
}
