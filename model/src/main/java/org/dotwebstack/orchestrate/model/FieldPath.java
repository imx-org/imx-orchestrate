package org.dotwebstack.orchestrate.model;

import java.util.Arrays;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Singular;

@Getter
@Builder(toBuilder = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class FieldPath {

  private static final String PATH_SEPARATOR = "/";

  @Singular
  private final List<String> segments;

  public String getFirstSegment() {
    return segments.get(0);
  }

  public boolean isLeaf() {
    return segments.size() == 1;
  }

  public FieldPath withoutFirstSegment() {
    return new FieldPath(segments.subList(1, segments.size()));
  }

  public static FieldPath fromString(String path) {
    return builder()
        .segments(Arrays.asList(path.split(PATH_SEPARATOR)))
        .build();
  }
}
