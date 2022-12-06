package org.dotwebstack.orchestrate.model.mapping;

import jakarta.validation.constraints.NotEmpty;
import java.util.Arrays;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

@Getter
@Builder(toBuilder = true)
public class FieldPath {

  private static final String PATH_SEPARATOR = "/";

  @NotEmpty
  @Singular
  private final List<String> segments;

  public static FieldPath fromString(String path) {
    return builder()
        .segments(Arrays.asList(path.split(PATH_SEPARATOR)))
        .build();
  }
}
