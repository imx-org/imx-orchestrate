package org.dotwebstack.orchestrate.ext.spatial;

import static org.dotwebstack.orchestrate.ext.spatial.GeometryExtension.AS_GEOJSON;
import static org.dotwebstack.orchestrate.ext.spatial.GeometryExtension.AS_WKB;
import static org.dotwebstack.orchestrate.ext.spatial.GeometryExtension.AS_WKT;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import java.util.Base64;
import org.dotwebstack.orchestrate.engine.OrchestrateException;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKBWriter;
import org.locationtech.jts.io.geojson.GeoJsonWriter;

class GeometryFetcher implements DataFetcher<Object> {

  private final GeoJsonWriter geoJsonWriter = new GeoJsonWriter();

  private final WKBWriter wkbWriter = new WKBWriter();

  public GeometryFetcher() {
    geoJsonWriter.setEncodeCRS(false);
  }

  @Override
  public Object get(DataFetchingEnvironment environment) {
    var source = environment.getSource();
    var fieldName = environment.getField()
        .getName();

    if (source instanceof Geometry geometry) {
      return switch (fieldName) {
        case AS_GEOJSON -> geoJsonWriter.write(geometry);
        case AS_WKB -> Base64.getEncoder()
            .encodeToString(wkbWriter.write(geometry));
        case AS_WKT -> geometry.toString();
        default -> throw new OrchestrateException("Invalid field name: " + fieldName);
      };
    }

    throw new OrchestrateException("Source value is not a JTS Geometry.");
  }
}
