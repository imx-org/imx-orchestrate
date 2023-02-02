package org.dotwebstack.orchestrate.source;

import java.util.Map;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder(toBuilder = true)
public final class ObjectRequest extends AbstractDataRequest {

  private final Map<String, Object> objectKey;
}
