package org.dotwebstack.orchestrate.source;

import java.util.Collection;
import java.util.Map;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder(toBuilder = true)
public final class BatchRequest extends AbstractDataRequest {

  private final Collection<Map<String, Object>> objectKeys;
}
