package nl.geostandaarden.imx.orchestrate.source;

import java.util.List;
import nl.geostandaarden.imx.orchestrate.model.ObjectType;

public interface DataRequest {

  ObjectType getObjectType();

  List<SelectedProperty> getSelectedProperties();
}
