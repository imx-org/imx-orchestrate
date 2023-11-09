package nl.geostandaarden.imx.orchestrate.engine.exchange;

import java.util.Set;
import nl.geostandaarden.imx.orchestrate.model.ObjectType;

public interface DataRequest {

  ObjectType getObjectType();

  Set<SelectedProperty> getSelectedProperties();
}
