package org.dotwebstack.orchestrate.engine.schema;

import com.google.common.base.CaseFormat;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SchemaUtil {

  public static String toLowerCamelCase(String input) {
    return CaseFormat.UPPER_UNDERSCORE
        .converterTo(CaseFormat.LOWER_CAMEL)
        .convert(input);
  }
}
