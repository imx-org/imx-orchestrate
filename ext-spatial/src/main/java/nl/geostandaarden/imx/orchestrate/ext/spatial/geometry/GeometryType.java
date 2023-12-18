package nl.geostandaarden.imx.orchestrate.ext.spatial.geometry;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import java.util.Map;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import nl.geostandaarden.imx.orchestrate.ext.spatial.SpatialException;
import nl.geostandaarden.imx.orchestrate.model.ModelException;
import nl.geostandaarden.imx.orchestrate.model.Path;
import nl.geostandaarden.imx.orchestrate.model.filters.FilterExpression;
import nl.geostandaarden.imx.orchestrate.model.filters.FilterOperator;
import nl.geostandaarden.imx.orchestrate.model.types.ValueType;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.io.geojson.GeoJsonReader;

@ToString
@RequiredArgsConstructor
public class GeometryType implements ValueType {

  public static final String TYPE_NAME = "Geometry";

  public static final int DEFAULT_SRID = 4326;

  private final ObjectMapper objectMapper = new JsonMapper();

  private final GeoJsonReader geoJsonReader = new GeoJsonReader();

  @Getter
  private final int srid;

  public GeometryType() {
    this(DEFAULT_SRID);
  }

  @Override
  public String getName() {
    return TYPE_NAME;
  }

  @Override
  public Object mapSourceValue(Object sourceValue) {
    if (sourceValue instanceof Map<?, ?>) {
      try {
        var jsonStr = objectMapper.writeValueAsString(sourceValue);
        var geometry = geoJsonReader.read(jsonStr);
        geometry.setSRID(srid);

        return geometry;
      } catch (JsonProcessingException | ParseException e) {
        throw new SpatialException("Failed mapping geometry value.", e);
      }
    }

    throw new SpatialException("Failed mapping geometry value.");
  }

  @Override
  public Object mapLineageValue(Object value) {
    if (value instanceof Map<?, ?>) {
      return mapSourceValue(value).toString();
    }

    if (value instanceof Geometry geometry) {
      return geometry.toString();
    }

    throw new SpatialException("Failed mapping lineage value.");
  }

  @Override
  public FilterExpression createFilterExpression(Path path, Map<String, Object> inputValue) {
    var wktReader = new WKTReader(new GeometryFactory(new PrecisionModel(), srid));
    Geometry geometry;

    if (inputValue.size() > 1) {
      throw new ModelException("Combining multiple spatial operators is not supported.");
    }

    var firstEntry = inputValue.entrySet()
        .iterator()
        .next();

    if (!(firstEntry.getValue() instanceof String)) {
      throw new ModelException("Spatial filter value is not a string.");
    }

    try {
      geometry = wktReader.read(firstEntry.getValue().toString());
    } catch (ParseException e) {
      throw new SpatialException("Failed parsing geometry", e);
    }

    if (!GeometryTypeFactory.FILTER_OPERATOR_TYPES.contains(firstEntry.getKey())) {
      throw new ModelException("Spatial operator is unknown: " + firstEntry.getKey());
    }

    var operator = FilterOperator.builder()
        .type(firstEntry.getKey())
        .build();

    return FilterExpression.builder()
        .path(path)
        .operator(operator)
        .value(geometry)
        .build();
  }
}
