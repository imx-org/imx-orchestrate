package org.dotwebstack.orchestrate.engine.source;

import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import org.dotwebstack.orchestrate.model.types.ObjectType;

@Getter
@Builder(toBuilder = true)
public final class ObjectRequest implements DataRequest {

  private final ObjectType objectType;

  private final Map<String, Object> objectKey;

  @Singular
  private final List<SelectedField> selectedFields;
}
