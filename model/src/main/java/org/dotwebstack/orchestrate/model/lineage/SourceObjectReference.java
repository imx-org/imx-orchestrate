package org.dotwebstack.orchestrate.model.lineage;

import java.util.Map;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(toBuilder = true)
public class SourceObjectReference {

  private final String objectType;

  private final Map<String, Object> objectKey;
}
