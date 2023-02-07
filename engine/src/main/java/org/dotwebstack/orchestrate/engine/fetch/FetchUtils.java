package org.dotwebstack.orchestrate.engine.fetch;

import static graphql.introspection.Introspection.INTROSPECTION_SYSTEM_FIELDS;

import graphql.schema.SelectedField;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.dotwebstack.orchestrate.model.ObjectType;
import org.dotwebstack.orchestrate.model.Property;
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

  public static boolean isIntrospectionField(SelectedField selectedField) {
    return INTROSPECTION_SYSTEM_FIELDS.contains(selectedField.getName());
  }

  @SuppressWarnings("unchecked")
  public static <T> T cast(Object value) {
    return (T) value;
  }
}
