package nl.geostandaarden.imx.orchestrate.ext.spatial.geometry;

import java.util.Map;
import java.util.Set;
import nl.geostandaarden.imx.orchestrate.model.ModelException;
import nl.geostandaarden.imx.orchestrate.model.types.ValueTypeFactory;

public class GeometryTypeFactory implements ValueTypeFactory<GeometryType> {

  public static final Set<String> FILTER_OPERATOR_TYPES = Set.of("intersects", "touches", "contains", "within");

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

  @Override
  public Set<String> getSupportedFilterTypes() {
    return FILTER_OPERATOR_TYPES;
  }
}
