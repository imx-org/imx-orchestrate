package org.dotwebstack.orchestrate.source;

import static java.util.Collections.emptyList;

import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.dotwebstack.orchestrate.model.types.Field;

@Getter
@RequiredArgsConstructor
public final class SelectedField {

  private final Field field;

  private final List<SelectedField> selectedFields;

  public SelectedField(Field field) {
    this(field, emptyList());
  }
}
