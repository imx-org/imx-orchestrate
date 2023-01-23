package org.dotwebstack.orchestrate.engine.schema;

import static org.dotwebstack.orchestrate.engine.schema.SchemaConstants.QUERY_TYPE;

import graphql.schema.FieldCoordinates;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SchemaUtil {

  public static FieldCoordinates queryField(String name) {
    return FieldCoordinates.coordinates(QUERY_TYPE, name);
  }
}
