package org.dotwebstack.orchestrate.ext.spatial;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import java.util.Map;
import lombok.ToString;
import org.dotwebstack.orchestrate.engine.OrchestrateException;
import org.dotwebstack.orchestrate.model.types.ScalarType;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.geojson.GeoJsonReader;

@ToString
public class GeometryType implements ScalarType<Geometry> {

  private final ObjectMapper objectMapper = new JsonMapper();

  private final GeoJsonReader geoJsonReader = new GeoJsonReader();

  @Override
  public String getName() {
    return Geometry.class.getSimpleName();
  }

  @Override
  public Class<Geometry> getJavaType() {
    return Geometry.class;
  }

  @Override
  public Object mapSourceValue(Object sourceValue) {
    if (sourceValue instanceof Map<?, ?>) {
      try {
        var jsonStr = objectMapper.writeValueAsString(sourceValue);
        return geoJsonReader.read(jsonStr);
      } catch (JsonProcessingException | ParseException e) {
        throw new OrchestrateException("Failed mapping geometry value.", e);
      }
    }

    throw new OrchestrateException("Failed mapping geometry value.");
  }
}
