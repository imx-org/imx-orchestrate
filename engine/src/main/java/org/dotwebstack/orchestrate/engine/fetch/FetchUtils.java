package org.dotwebstack.orchestrate.engine.fetch;

import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.dotwebstack.orchestrate.model.types.Field;
import org.dotwebstack.orchestrate.model.types.ObjectType;
import org.dotwebstack.orchestrate.source.SelectedField;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FetchUtils {

  public static UnaryOperator<Map<String, Object>> keyExtractor(ObjectType objectType) {
    return data -> objectType.getIdentityFields()
        .stream()
        .collect(Collectors.toMap(Field::getName, field -> data.get(field.getName())));
  }

  public static List<SelectedField> selectIdentifyFields(ObjectType objectType) {
    return objectType.getIdentityFields()
        .stream()
        .map(SelectedField::new)
        .toList();
  }
}
