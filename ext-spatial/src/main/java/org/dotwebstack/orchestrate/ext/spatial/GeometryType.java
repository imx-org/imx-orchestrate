package org.dotwebstack.orchestrate.ext.spatial;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import java.util.Map;
import lombok.ToString;
import org.dotwebstack.orchestrate.engine.OrchestrateException;
import org.dotwebstack.orchestrate.ext.spatial.filters.IntersectsOperatorType;
import org.dotwebstack.orchestrate.model.AttributeType;
import org.dotwebstack.orchestrate.model.Path;
import org.dotwebstack.orchestrate.model.filters.FilterDefinition;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.io.geojson.GeoJsonReader;

@ToString
public class GeometryType implements AttributeType {

  private final ObjectMapper objectMapper = new JsonMapper();

  private final GeoJsonReader geoJsonReader = new GeoJsonReader();

  @Override
  public String getName() {
    return "Geometry";
  }

  @Override
  public Object mapSourceValue(Object sourceValue) {
    if (sourceValue instanceof Map<?, ?>) {
      try {
        var jsonStr = objectMapper.writeValueAsString(sourceValue);
        var geometry = geoJsonReader.read(jsonStr);

        // TODO: Dynamic SRID
        geometry.setSRID(28992);

        return geometry;
      } catch (JsonProcessingException | ParseException e) {
        throw new OrchestrateException("Failed mapping geometry value.", e);
      }
    }

    throw new OrchestrateException("Failed mapping geometry value.");
  }

  @Override
  public Object mapLineageValue(Object value) {
    if (value instanceof Geometry geometry) {
      return geometry.toString();
    }

    throw new OrchestrateException("Failed mapping lineage value.");
  }

  @Override
  public FilterDefinition createFilterDefinition(Path path, Object inputValue) {
    var firstEntry = ((Map<String, Object>) inputValue).entrySet()
        .iterator()
        .next();

    var wkt = (String) ((Map<String, Object>) firstEntry.getValue()).get("fromWKT");
    var wktReader = new WKTReader(new GeometryFactory(new PrecisionModel(), 28992));

    Geometry geometry;

    try {
      geometry = wktReader.read(wkt);
    } catch (ParseException e) {
      throw new OrchestrateException("Failed parsing geometry", e);
    }

    return FilterDefinition.builder()
        .path(path)
        .operator(new IntersectsOperatorType().create(Map.of()))
        .value(geometry)
        .build();
  }
}
