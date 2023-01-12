package org.dotwebstack.orchestrate.engine.schema;

import static org.dotwebstack.orchestrate.engine.schema.SchemaConstants.QUERY_TYPE;

import com.google.common.base.CaseFormat;
import graphql.schema.FieldCoordinates;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SchemaUtil {

  public static String toLowerCamelCase(String input) {
    return CaseFormat.UPPER_UNDERSCORE
        .converterTo(CaseFormat.LOWER_CAMEL)
        .convert(input);
  }

  public static FieldCoordinates queryField(String name) {
    return FieldCoordinates.coordinates(QUERY_TYPE, name);
  }
}
