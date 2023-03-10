package org.dotwebstack.orchestrate.ext.spatial;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;
import org.locationtech.jts.geom.Geometry;

public class GeometrySerializer extends StdSerializer<Geometry> {

  public GeometrySerializer() {
    this(null);
  }

  public GeometrySerializer(Class<Geometry> type) {
    super(type);
  }

  @Override
  public void serialize(Geometry geometry, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
    jsonGenerator.writeString(geometry.toString());
  }
}
