package org.dotwebstack.orchestrate.source;

import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import org.dotwebstack.orchestrate.model.types.ObjectType;

@Getter
@Builder(toBuilder = true)
public final class ObjectRequest implements DataRequest {

  private final ObjectType objectType;

  private final Map<String, Object> objectKey;

  private final List<SelectedField> selectedFields;
}
