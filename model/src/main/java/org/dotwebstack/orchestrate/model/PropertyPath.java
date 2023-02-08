package org.dotwebstack.orchestrate.model;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Singular;

@Getter
@EqualsAndHashCode
@Builder(toBuilder = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class PropertyPath {

  private static final String PATH_SEPARATOR = "/";

  @Singular
  private final List<String> segments;

  public String getFirstSegment() {
    return segments.get(0);
  }

  public boolean isLeaf() {
    return segments.size() == 1;
  }

  public PropertyPath withoutFirstSegment() {
    return new PropertyPath(segments.subList(1, segments.size()));
  }

  public PropertyPath prependSegment(String segment) {
    return new PropertyPath(Stream.concat(Stream.of(segment), segments.stream()).toList());
  }

  public static PropertyPath fromString(String path) {
    var segments = path.split(PATH_SEPARATOR);

    return builder()
        .segments(Arrays.asList(segments))
        .build();
  }

  @Override
  public String toString() {
    return String.join(PATH_SEPARATOR, segments);
  }
}
