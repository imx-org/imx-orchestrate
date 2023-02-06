package org.dotwebstack.orchestrate.source;

import java.util.List;
import org.dotwebstack.orchestrate.model.ObjectType;

public interface DataRequest {

  ObjectType getObjectType();

  List<SelectedProperty> getSelectedProperties();
}
