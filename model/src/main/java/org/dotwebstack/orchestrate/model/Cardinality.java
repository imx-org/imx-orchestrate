package org.dotwebstack.orchestrate.model;

import java.util.regex.Pattern;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Cardinality {

  public static final Pattern CARDINALITY_PATTERN = Pattern.compile("^(?:(\\d+)\\.{2})?(\\d+|\\*)$");

  public static final int INFINITE = -1;

  public static final Cardinality REQUIRED = Cardinality.of(1, 1);

  public static final Cardinality OPTIONAL = Cardinality.of(0, 1);

  public static final Cardinality MULTI = Cardinality.of(0, INFINITE);

  private final int min;

  private final int max;

  public boolean isSingular() {
    return max == 1;
  }

  public static Cardinality of(int min, int max) {
    return new Cardinality(min, max);
  }

  public static Cardinality fromString(String cardinality) {
    var matcher = CARDINALITY_PATTERN.matcher(cardinality);

    if (matcher.find()) {
      var max = matcher.group(2)
          .equals("*") ? INFINITE : Integer.parseInt(matcher.group(2));
      var min = matcher.group(1) == null ? max : Integer.parseInt(matcher.group(1));

      if (max > INFINITE && min > max) {
        throw new IllegalArgumentException(String.format("Min is larger than max in cardinality: %s", cardinality));
      }

      return new Cardinality(min, max);
    }

    throw new IllegalArgumentException(String.format("Could not parse cardinality: %s", cardinality));
  }
}
