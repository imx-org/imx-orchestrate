package org.dotwebstack.orchestrate.engine.fetch;

import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.dotwebstack.orchestrate.engine.OrchestrateException;
import org.dotwebstack.orchestrate.model.FieldPath;
import org.dotwebstack.orchestrate.model.types.Field;
import org.dotwebstack.orchestrate.model.types.ObjectType;
import org.dotwebstack.orchestrate.model.types.ScalarType;
import org.dotwebstack.orchestrate.source.SelectedField;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FetchUtils {

  public static UnaryOperator<Map<String, Object>> keyExtractor(ObjectType objectType) {
    return data -> objectType.getIdentityFields()
        .stream()
        .collect(Collectors.toMap(Field::getName, field -> data.get(field.getName())));
  }

  public static List<SelectedField> selectFields(ObjectType sourceType, List<FieldPath> sourcePaths) {
    return sourcePaths.stream()
        .flatMap(sourcePath -> selectField(sourceType, sourcePath).stream())
        .toList();
  }

  public static List<SelectedField> selectField(ObjectType sourceType, FieldPath sourcePath) {
    var sourceField = sourceType.getField(sourcePath.getSegments()
        .get(0));

    if (sourceField.getType() instanceof ScalarType<?>) {
      return List.of(SelectedField.builder()
          .field(sourceField)
          .build());
    }

    if (sourceField.getType() instanceof ObjectType fieldType) {
      return List.of(SelectedField.builder()
          .field(sourceField)
          .selectedFields(selectIdentifyFields(fieldType))
          .build());
    }

    throw new OrchestrateException("Could not map selected fields.");
  }

  public static List<SelectedField> selectIdentifyFields(ObjectType sourceType) {
    return sourceType.getIdentityFields()
        .stream()
        .map(identityField -> SelectedField.builder()
            .field(identityField)
            .build())
        .toList();
  }
}
