package org.dotwebstack.orchestrate.engine.source;

import graphql.language.ObjectField;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(toBuilder = true)
public final class SelectedField {

  private final String name;

  private final ObjectField objectField;

  private final List<SelectedField> childFields;
}
