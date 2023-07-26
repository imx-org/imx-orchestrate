package nl.geostandaarden.imx.orchestrate.ext.spatial;

import java.util.Map;
import nl.geostandaarden.imx.orchestrate.model.ModelException;
import nl.geostandaarden.imx.orchestrate.model.types.ValueTypeFactory;

public class GeometryTypeFactory implements ValueTypeFactory<GeometryType> {

  @Override
  public String getTypeName() {
    return GeometryType.TYPE_NAME;
  }

  @Override
  public GeometryType create(Map<String, Object> options) {
    var srid = options.getOrDefault("srid", GeometryType.DEFAULT_SRID);

    if (srid instanceof Integer sridInt) {
      return new GeometryType(sridInt);
    }

    throw new ModelException("SRID is not an integer.");
  }
}
