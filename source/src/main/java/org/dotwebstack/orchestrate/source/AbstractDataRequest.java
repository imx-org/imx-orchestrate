package org.dotwebstack.orchestrate.source;

import java.util.List;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.dotwebstack.orchestrate.model.ObjectType;

@Getter
@SuperBuilder(toBuilder = true)
public abstract class AbstractDataRequest implements DataRequest {

  private final ObjectType objectType;

  private final List<SelectedProperty> selectedProperties;
}
