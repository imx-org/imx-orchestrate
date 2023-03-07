package org.dotwebstack.orchestrate.model;

import java.util.regex.Pattern;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ObjectTypeRef {

  private static final Pattern PATTERN = Pattern.compile("^(?:(\\w+):)?(\\w+)$");

  private final String modelAlias;

  private final String name;

  public static ObjectTypeRef forType(String name) {
    return new ObjectTypeRef(null, name);
  }

  public static ObjectTypeRef forType(String modelAlias, String name) {
    return new ObjectTypeRef(modelAlias, name);
  }

  public static ObjectTypeRef fromString(String input) {
    var matcher = PATTERN.matcher(input);

    if (matcher.find()) {
      return new ObjectTypeRef(matcher.group(1), matcher.group(2));
    }

    throw new IllegalArgumentException("Object type references must match pattern: " + PATTERN);
  }
}
