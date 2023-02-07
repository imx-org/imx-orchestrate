package org.dotwebstack.orchestrate.model;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Singular;
import org.dotwebstack.orchestrate.model.types.ObjectTypeRef;

@Getter
@Builder(toBuilder = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class PropertyPath {

  private static final Pattern PATH_PATTERN = Pattern.compile("^(?:(\\w+):)?([\\w/]+)$");

  private static final String PATH_SEPARATOR = "/";

  @Singular
  private final List<String> segments;

  private final ObjectTypeRef origin;

  public String getFirstSegment() {
    return segments.get(0);
  }

  public boolean isLeaf() {
    return segments.size() == 1;
  }

  public boolean hasOrigin() {
    return origin != null;
  }

  public PropertyPath withoutFirstSegment() {
    return new PropertyPath(segments.subList(1, segments.size()), origin);
  }

  public static PropertyPath fromString(String path) {
    var pathMatcher = PATH_PATTERN.matcher(path);

    if (pathMatcher.find()) {
      var segments = pathMatcher.group(2)
          .split(PATH_SEPARATOR);

      var origin = Optional.ofNullable(pathMatcher.group(1))
          .map(ObjectTypeRef::forType)
          .orElse(null);

      return builder()
          .segments(Arrays.asList(segments))
          .origin(origin)
          .build();
    }

    throw new IllegalArgumentException("Could not parse field path.");
  }

  @Override
  public String toString() {
    var pathString = String.join(PATH_SEPARATOR, segments);

    if (origin == null) {
      return pathString;
    }

    return origin.toString()
        .concat(":")
        .concat(pathString);
  }
}
