package org.dotwebstack.orchestrate.model.lineage;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(toBuilder = true)
public final class SourceProperty implements PropertyLineage {

  private final SourceObjectReference subject;

  private final String property;

  private final List<String> propertyPath;

  private final Object value;
}
