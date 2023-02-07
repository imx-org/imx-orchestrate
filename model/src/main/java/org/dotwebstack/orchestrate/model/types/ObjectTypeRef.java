package org.dotwebstack.orchestrate.model.types;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.dotwebstack.orchestrate.model.AttributeType;

@Getter
@ToString
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ObjectTypeRef implements AttributeType {

  private final String name;

  public static ObjectTypeRef forType(String name) {
    return new ObjectTypeRef(name);
  }
}
