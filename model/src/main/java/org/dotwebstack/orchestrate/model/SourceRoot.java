package org.dotwebstack.orchestrate.model;

import java.util.regex.Pattern;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class SourceRoot {

  private static final Pattern PATTERN = Pattern.compile("^(\\w+):(\\w+)$");

  private final String modelAlias;

  private final String objectType;

  public static SourceRoot fromString(String input) {
    var matcher = PATTERN.matcher(input);

    if (matcher.find()) {
      return new SourceRoot(matcher.group(1), matcher.group(2));
    }

    throw new IllegalArgumentException("Object type references must match pattern: " + PATTERN);
  }
}
