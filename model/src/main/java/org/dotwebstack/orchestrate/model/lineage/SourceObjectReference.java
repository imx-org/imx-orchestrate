package org.dotwebstack.orchestrate.model.lineage;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(toBuilder = true)
public class SourceObjectReference {

  private final String objectType;

  private final String objectKey;
}
