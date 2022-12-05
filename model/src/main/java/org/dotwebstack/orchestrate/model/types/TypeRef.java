package org.dotwebstack.orchestrate.model.types;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class TypeRef implements Type {

  private final String name;

  public static TypeRef forType(String name) {
    return new TypeRef(name);
  }
}
