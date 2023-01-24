package org.dotwebstack.orchestrate.source;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import org.dotwebstack.orchestrate.model.types.Field;

@Getter
@Builder(toBuilder = true)
public final class SelectedField {

  private final Field field;

  private final List<SelectedField> selectedFields;
}
