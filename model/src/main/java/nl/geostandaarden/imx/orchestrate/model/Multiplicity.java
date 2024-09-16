package nl.geostandaarden.imx.orchestrate.model;

import java.util.regex.Pattern;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Multiplicity {

  public static final Pattern MULTIPLICITY_PATTERN = Pattern.compile("^(?:(\\d+)\\.{2})?(\\d+|\\*)$");

  public static final int INFINITE = -1;

  public static final Multiplicity REQUIRED = Multiplicity.of(1, 1);

  public static final Multiplicity OPTIONAL = Multiplicity.of(0, 1);

  public static final Multiplicity MULTI = Multiplicity.of(0, INFINITE);

  private final int min;

  private final int max;

  public boolean isSingular() {
    return max == 1;
  }

  public static Multiplicity of(int min, int max) {
    return new Multiplicity(min, max);
  }

  public static Multiplicity fromString(String multiplicity) {
    var matcher = MULTIPLICITY_PATTERN.matcher(multiplicity);

    if (matcher.find()) {
      var max = matcher.group(2)
          .equals("*") ? INFINITE : Integer.parseInt(matcher.group(2));
      var min = matcher.group(1) == null ? max : Integer.parseInt(matcher.group(1));

      if (max > INFINITE && min > max) {
        throw new IllegalArgumentException(String.format("Min is larger than max in multiplicity: %s", multiplicity));
      }

      return new Multiplicity(min, max);
    }

    throw new IllegalArgumentException(String.format("Could not parse multiplicity: %s", multiplicity));
  }
}
