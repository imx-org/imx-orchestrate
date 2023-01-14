package org.dotwebstack.orchestrate.engine.source;

import java.util.List;
import org.dotwebstack.orchestrate.model.types.ObjectType;

public interface DataRequest {

  ObjectType getObjectType();

  List<SelectedField> getSelectedFields();
}
