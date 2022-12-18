package org.dotwebstack.orchestrate.engine.fetch;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

@Getter
@Builder(toBuilder = true)
public final class SelectedField {

  private final String name;

  @Singular
  private final List<SelectedField> nestedFields;
}
